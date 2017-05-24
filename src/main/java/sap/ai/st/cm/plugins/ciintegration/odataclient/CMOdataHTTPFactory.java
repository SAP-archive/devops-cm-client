package sap.ai.st.cm.plugins.ciintegration.odataclient;

import java.net.URI;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import org.apache.olingo.commons.api.http.HttpMethod;

public class CMOdataHTTPFactory extends BasicAuthHttpClientFactory {

    private final CookieStore cookieStore;

    public CMOdataHTTPFactory(String username, String password) {
        
        super(username, password);
        
        this.cookieStore = new BasicCookieStore();
    }

    @Override
    public DefaultHttpClient create(final HttpMethod method, final URI uri) {
        
        final DefaultHttpClient httpClient = super.create(method, uri);

        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);

        httpClient.setCookieStore(this.cookieStore);

        return httpClient;
    }
}
