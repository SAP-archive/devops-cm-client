package com.sap.cmclient.http;

import static com.google.common.primitives.Ints.asList;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;

import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.dto.TransportMarshaller;

public class CMODataClient {

    private final URI endpoint;
    private final HttpClientFactory clientFactory;

    public CMODataClient(String endpoint, String user, String password) throws URISyntaxException {

        this.endpoint = new URI(endpoint);

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


    // args[0] "http://wdflbmd16301.wdf.sap.corp:8000/sap/opu/odata/SAP/SCTS_CLOUD_API_ODATA_SRV/Transports(\'A5DK900014\')"
    // args[1] ODATA
    // args[2] <the password ...>
    
    // In order to run it with wire logging use:
    // -Dorg.slf4j.simpleLogger.defaultLogLevel=trace
    // -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog
    // -Dorg.apache.commons.logging.simplelog.showdatetime=true
    // -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG
    // -Dorg.apache.commons.logging.simplelog.log.org.apache.http.wire=DEBUG

    public final static void main(String[] args) throws Exception {
        Transport transport = new CMODataClient(args[0], args[1], args[2]).getTransport("A5DK900014");
        System.out.println(transport);
    }

    public Transport getTransport(String transportId) throws IOException, EntityProviderException, EdmException, UnexpectedHttpResponseException {

        final String entityKey = "Transports";

        try (CloseableHttpClient client = clientFactory.createClient()) {
            HttpUriRequest get = new HttpGet(endpoint + "/" + entityKey + "('" + transportId + "')");
            get.setHeader("Accept-Encoding", "identity");
            try (CloseableHttpResponse response = client.execute(get)) {
                checkStatusCode(response, SC_OK, SC_NOT_FOUND, 500); // 500 is currently returned in case the transport cannot be found.
                if(Arrays.asList(SC_OK).contains(response.getStatusLine().getStatusCode())) {
                    return TransportMarshaller.get(EntityProvider.readEntry("application/xml",
                                        getEntityDataModel().getDefaultEntityContainer().getEntitySet(entityKey),
                                        response.getEntity().getContent(),
                                        EntityProviderReadProperties.init().build()));

                }
            }

            return null;
        }
    }

    private Edm getEntityDataModel() throws IOException, EntityProviderException, UnexpectedHttpResponseException {

        try (CloseableHttpClient edmClient = clientFactory.createClient()) {
            try (CloseableHttpResponse response = edmClient.execute(new HttpGet(endpoint.toASCIIString() + "/" + "$metadata"))) {
                checkStatusCode(response, SC_OK);
                return EntityProvider.readMetadata(response.getEntity().getContent(), false);
            }
        }
    }

    private static void checkStatusCode(HttpResponse response, int...  expected) throws UnexpectedHttpResponseException {

        if( ! asList(expected).contains(response.getStatusLine().getStatusCode())) {
            throw new UnexpectedHttpResponseException(format("Unexpected response code %d", response.getStatusLine().getStatusCode()),
                                            response.getStatusLine());
        }
    }

}
