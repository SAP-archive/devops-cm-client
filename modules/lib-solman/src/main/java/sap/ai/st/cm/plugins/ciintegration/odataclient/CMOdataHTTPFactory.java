package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static java.lang.String.format;

import java.net.URI;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import org.apache.olingo.commons.api.http.HttpMethod;

/**
 * Our own factory for http clients.
 * We set
 *   <ul>
 *     <li>We set a cookie store
 *     <li>We set the USER_AGENT header in order to be able to identify requests performed
 *         by the client (and the corresponding client version) on the server side.
 *   </ul>
 */
public class CMOdataHTTPFactory extends BasicAuthHttpClientFactory {

    private final CookieStore cookieStore;

    public CMOdataHTTPFactory(String username, String password) {
        
        super(username, password);
        
        this.cookieStore = new BasicCookieStore();
    }

    @Override
    public DefaultHttpClient create(final HttpMethod method, final URI uri) {
        
        final DefaultHttpClient httpClient = super.create(method, uri);

        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
                format("SAP CM Client/%s based on %s", CMODataClient.getShortVersion(), USER_AGENT));

        httpClient.setCookieStore(this.cookieStore);

        return httpClient;
    }
}
