package com.sap.cmclient.http;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.primitives.Ints.asList;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sap.cmclient.dto.Transport;

public class CMODataAbapClient {

    private final TransportRequestBuilder requestBuilder;
    private final URI endpoint;
    private final HttpClientFactory clientFactory;
    private String csrfToken = null;
    private Edm dataModel = null;

    public CMODataAbapClient(String endpoint, String user, String password) throws URISyntaxException {

        this.endpoint = new URI(endpoint);
        this.requestBuilder = new TransportRequestBuilder(this.endpoint);

        // the same instance needs to be used as long as we are in the same session. Hence multiple
        // clients must share the same cookie store. Reason: we are logged on with the first request
        // and get a cookie. That cookie - expressing the user has already been logged in - needs to be
        // present in all subsequent requests.
        CookieStore sessionCookieStore = new BasicCookieStore();

        BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        basicCredentialsProvider.setCredentials(
                new AuthScope(this.endpoint.getHost(), this.endpoint.getPort()),
                new UsernamePasswordCredentials(user, password));

        this.clientFactory = new HttpClientFactory(sessionCookieStore, basicCredentialsProvider);
    }

  public Transport getTransport(String transportId) throws IOException, EntityProviderException, EdmException, UnexpectedHttpResponseException {

      try (CloseableHttpClient client = clientFactory.createClient()) {
          HttpUriRequest get = requestBuilder.getTransport(transportId);
          try(CloseableHttpResponse response = client.execute(get)) {
              checkStatusCodeAndFail(response, SC_OK, SC_NOT_FOUND, 500); // 500 is currently returned in case the transport cannot be found.a
              return getTransport(response);
          }
      }
  }

  private Transport getTransport(CloseableHttpResponse response) throws UnsupportedOperationException, IOException, EntityProviderException, EdmException, UnexpectedHttpResponseException {

      if(! checkStatusCode(response, HttpStatus.SC_OK)) {
          return null;
      }

      Header[] contentType = response.getHeaders(HttpHeaders.CONTENT_TYPE);
      try(InputStream content = response.getEntity().getContent()) {
          return new Transport(EntityProvider.readEntry(contentType[0].getValue(),
                                         getEntityDataModel().getDefaultEntityContainer()
                                             .getEntitySet(TransportRequestBuilder.getEntityKey()),
                                         content,
                                         EntityProviderReadProperties.init().build()));
      }
  }

  public void updateTransport(Transport transport) throws IOException, URISyntaxException, ODataException, UnexpectedHttpResponseException
  {
    Edm edm = getEntityDataModel();
    try (CloseableHttpClient client = clientFactory.createClient()) {
      
      HttpPut put = requestBuilder.updateTransport(transport.getTransportID());
      put.setHeader("x-csrf-token", getCSRFToken());
      EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
      EdmEntitySet entitySet = entityContainer.getEntitySet(TransportRequestBuilder.getEntityKey());
      URI rootUri = new URI(endpoint.toASCIIString() + "/");
      EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(rootUri).build();
      ODataResponse response = null;
      try {
          response = EntityProvider.writeEntry(put.getHeaders(HttpHeaders.CONTENT_TYPE)[0].getValue(), entitySet,transport.getValueMap(), properties);
          put.setEntity(EntityBuilder.create().setStream(response.getEntityAsStream()).build());
          try (CloseableHttpResponse httpResponse = client.execute(put)) {
              checkStatusCodeAndFail(httpResponse, SC_OK, HttpStatus.SC_NO_CONTENT);
          }
      }  finally {
          if(response != null) response.close();
      }
    }
  }
  public Transport createTransport(Map<String, Object> transport) throws IOException, URISyntaxException, ODataException, UnexpectedHttpResponseException
  {
    Edm edm = getEntityDataModel();
    try (CloseableHttpClient client = clientFactory.createClient()) {
      HttpPost post = requestBuilder.createTransport();
      post.setHeader("x-csrf-token", getCSRFToken());
      EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
      EdmEntitySet entitySet = entityContainer.getEntitySet(TransportRequestBuilder.getEntityKey());
      URI rootUri = new URI(endpoint.toASCIIString() + "/");
      EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(rootUri).build();
      ODataResponse response = null;

      try {
        response = EntityProvider.writeEntry(post.getHeaders(HttpHeaders.CONTENT_TYPE)[0].getValue(), entitySet, transport, properties);
        post.setEntity(EntityBuilder.create().setStream(response.getEntityAsStream()).build());

        try(CloseableHttpResponse httpResponse = client.execute(post)) {
          Header[] contentType = httpResponse.getHeaders(HttpHeaders.CONTENT_TYPE);
          checkStatusCodeAndFail(httpResponse, SC_CREATED);
          if (Arrays.asList(SC_CREATED).contains(httpResponse.getStatusLine().getStatusCode())) {
            return new Transport(EntityProvider.readEntry(contentType[0].getValue(),
                       edm.getDefaultEntityContainer()
                      .getEntitySet(TransportRequestBuilder.getEntityKey()),
                        httpResponse.getEntity().getContent(), EntityProviderReadProperties.init().build()));
          }
        }
      } finally {
          if(response != null) response.close();
      }

      return null;
    }
  }

  public void deleteTransport(String id) throws EntityProviderException, IOException, UnexpectedHttpResponseException
  {
    try (CloseableHttpClient client = clientFactory.createClient()) {
      HttpDelete delete = requestBuilder.deleteTransport(id);
      delete.setHeader("x-csrf-token", getCSRFToken());
      try(CloseableHttpResponse response = client.execute(delete)) {
        checkStatusCodeAndFail(response, HttpStatus.SC_NO_CONTENT);
      }
    }


  }

    public String upload(String transportId, File content) throws IOException, CMODataClientException, UnexpectedHttpResponseException {

        checkArgument(content.canRead(), format("Cannot read file '%s'.", content.getAbsolutePath()));

        try(InputStream c = new FileInputStream(content)) {
            return upload(transportId, c);
        }
    }

    public String upload(String transportId, InputStream content) throws IOException, CMODataClientException, UnexpectedHttpResponseException {

        checkArgument(! isNullOrEmpty(transportId), "TransportId is null or empty.");
        checkArgument(content != null, "No content provided for upload.");

        //
        // Below we work with a non-repeatable entity. This is fine since we can be sure the
        // request does not need to be repeated. Reason: the need for repeating a request inside
        // the client (transparent for the caller) is an authentification scenario (first request
        // without credentials, resulting in 401, repeated request with credentials). When uploading
        // the content we are already authenticated since we have to fetch the csrf token beforehand
        // in any case.
        //

        try (CloseableHttpClient client = clientFactory.createClient()) {
            HttpPost request = requestBuilder.upload(transportId);
            request.setEntity(new InputStreamEntity(content));
            request.addHeader("x-csrf-token", getCSRFToken());
            try (CloseableHttpResponse response = client.execute(request)) {
                checkStatusCodeAndFail(response, HttpStatus.SC_CREATED);
                return response.getHeaders("location")[0].getValue();
            }
        }
    }

    public Transport releaseTransport(String transportId) throws IOException, UnexpectedHttpResponseException, EntityProviderException, EdmException {
        try(CloseableHttpClient client = clientFactory.createClient()) {
            HttpGet request = requestBuilder.exportTransport(transportId);
            request.addHeader(HttpHeaders.ACCEPT, "application/xml");
            request.addHeader("x-csrf-token", getCSRFToken());
            try (CloseableHttpResponse response = client.execute(request)) {
                checkStatusCodeAndFail(response, HttpStatus.SC_OK);
                return getTransport(response);
            }
        }
    }

    public void importTransport(String systemId, String transportId) throws IOException, UnexpectedHttpResponseException {
        try(CloseableHttpClient client = clientFactory.createClient()) {
            HttpGet request = requestBuilder.importTransport(systemId, transportId);
            request.addHeader(HttpHeaders.ACCEPT, "application/xml");
            request.addHeader("x-csrf-token", getCSRFToken());
            try(CloseableHttpResponse response = client.execute(request)) {
                checkStatusCodeAndFail(response, HttpStatus.SC_OK);
            }
        }
    }

    private synchronized Edm getEntityDataModel() throws IOException, EntityProviderException, UnexpectedHttpResponseException {

        if(dataModel == null) {
            try (CloseableHttpClient edmClient = clientFactory.createClient()) {
                try (CloseableHttpResponse response = edmClient.execute(new HttpGet(endpoint.toASCIIString() + "/" + "$metadata"))) {
                    checkStatusCodeAndFail(response, SC_OK);
                    dataModel = EntityProvider.readMetadata(response.getEntity().getContent(), false);
                }
            }
        }

        return dataModel;
    }

    private synchronized String getCSRFToken() throws ClientProtocolException, IOException {

        if(this.csrfToken == null) {
            HttpGet httpGet = new HttpGet(this.endpoint);
            httpGet.addHeader("X-CSRF-Token", "Fetch");
            httpGet.addHeader(HttpHeaders.ACCEPT, "application/xml");
            try(CloseableHttpClient client = clientFactory.createClient()) {
                try(CloseableHttpResponse response = client.execute(httpGet)) {
                    Header[] csrfHeaders = response.getHeaders("x-csrf-token");
                    if(csrfHeaders.length != 1) throw new IllegalStateException("Multiple or no csrfHeaders received.");
                    this.csrfToken = csrfHeaders[0].getValue();
                }
            }
        }

        return this.csrfToken;
    }

    private static void checkStatusCodeAndFail(HttpResponse response, int...  expected) throws UnexpectedHttpResponseException {

        if(! checkStatusCode(response, expected)) {

            String reason;

            try(Reader content = new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8)) {
                // TODO hard coded char set is a best guess. Should be retrieved from response.
                reason = CharStreams.toString(content);
            } catch(IOException e) {
                reason = String.format("Cannot provide a reason string. %s caught while retrieving the reason. Message of that excepetion was: %s", e.getClass().getName(), e.getMessage());
            }

            throw new UnexpectedHttpResponseException(format("Unexpected response code '%d'. Reason: %s", response.getStatusLine().getStatusCode(), reason),
                    response.getStatusLine());
        }
    }

    private static boolean checkStatusCode(HttpResponse response, int...  expected) {
        return asList(expected).contains(response.getStatusLine().getStatusCode());
    }
}
