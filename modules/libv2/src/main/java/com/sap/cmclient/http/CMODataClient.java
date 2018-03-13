package com.sap.cmclient.http;

import static com.google.common.primitives.Ints.asList;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
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
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;

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
        new CMODataClient(args[0], args[1], args[2]).getTransport();
    }

    public void getTransport() throws IOException, EntityProviderException, EdmException {

        
        final Edm edm;
        try (CloseableHttpClient edmClient = clientFactory.createClient()) {

            edm = EntityProvider.readMetadata(
                    edmClient.execute(
                            new HttpGet(endpoint.toASCIIString() + "/" + "$metadata")
                    ).getEntity().getContent(), false);
        }

        final ODataEntry transport;

        try (CloseableHttpClient client = clientFactory.createClient()) {
            HttpUriRequest get = new HttpGet(endpoint + "/" + "Transports('A5DK900014')");
            HttpResponse response = client.execute(get);
            InputStream is = response.getEntity().getContent();
            checkStatusCode(response, SC_OK, SC_NOT_FOUND);

            transport = EntityProvider.readEntry("application/xml",
                                                 edm.getDefaultEntityContainer().getEntitySet("Transports"),
                                                 is,
                                                 EntityProviderReadProperties.init().build());
        }

        System.out.println(transport);
    }

    private static void checkStatusCode(HttpResponse response, int...  expected) {

        if( ! asList(expected).contains(response.getStatusLine().getStatusCode())) {
            // TODO: maybe dedicated exception ...
            throw new RuntimeException(String.format("Unexpected response code %d", response.getStatusLine().getStatusCode()));
        }
    }

}
