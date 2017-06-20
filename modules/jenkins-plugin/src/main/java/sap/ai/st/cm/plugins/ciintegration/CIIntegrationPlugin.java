package sap.ai.st.cm.plugins.ciintegration;

import hudson.Plugin;
import java.util.logging.Logger;

public class CIIntegrationPlugin extends Plugin {

    private final static Logger LOG = Logger.getLogger(CIIntegrationPlugin.class.getName());

    @Override
    public void start() throws Exception {
        LOG.info("starting SAP ChaRM plugin");
        super.start();
    }

    @Override
    public void stop() throws Exception {
        LOG.info("stopping SAP ChaRM plugin");
        super.stop();
    }   
}
