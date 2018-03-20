package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.junit.Test;

public class CMOdataHTTPFactoryTest {

    @Test
    public void testUserAgentStringContainsCMClientHint() throws Exception {
        HttpClient httpClient = new CMOdataHTTPFactory("me", "*****").create(HttpMethod.GET, new URI("http://example.org"));
        assertThat((String)httpClient.getParams().getParameter(CoreProtocolPNames.USER_AGENT),
            containsString("SAP CM Client"));
    }
}
