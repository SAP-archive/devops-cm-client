package sap.ai.st.cm.plugins.ciintegration.odataclient;

import org.apache.olingo.client.api.Configuration;
import org.apache.olingo.client.core.ConfigurationImpl;

public class MockHelper {

    private final static Configuration config = new ConfigurationImpl();

    static {
        config.setKeyAsSegment(false); // with that we get .../Changes('<ChangeId>'), otherwise .../Changes/'<ChangeId>'
    }

    public final static Configuration getConfiguration() {
        return config;
    }
}
