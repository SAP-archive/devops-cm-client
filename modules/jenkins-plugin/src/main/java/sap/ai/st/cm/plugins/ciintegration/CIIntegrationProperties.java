package sap.ai.st.cm.plugins.ciintegration;

import hudson.FilePath;
import java.io.IOException;
import java.util.Properties;

public class CIIntegrationProperties {

    public static final String DEVELOPMENT_TRANSPORT_ID = "DevelopmentTransportID";

    private final Properties Properties;
    private final FilePath FilePath;

    public CIIntegrationProperties(FilePath filePath) throws IOException, InterruptedException {

        this.Properties = new Properties();

        this.FilePath = new FilePath(filePath, "ciintegration.properties");

        if (!this.FilePath.exists()) {
            this.FilePath.write("", null);
        }

        this.Properties.load(this.FilePath.read());
    }

    public void setProperty(String key, String value) throws IOException, InterruptedException {

        this.Properties.setProperty(key, value);
        this.Properties.store(this.FilePath.write(), null);

    }

    public String getProperty(String key) {
        return this.Properties.getProperty(key);
    }

    public void setDevelopmentTransportID(String TransportID) throws IOException, InterruptedException {
        this.setProperty(CIIntegrationProperties.DEVELOPMENT_TRANSPORT_ID, TransportID);
    }

    public String getDevelopmentTransportID() {
        return this.getProperty(CIIntegrationProperties.DEVELOPMENT_TRANSPORT_ID);
    }
}
