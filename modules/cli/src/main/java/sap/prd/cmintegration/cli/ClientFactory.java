package sap.prd.cmintegration.cli;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

class ClientFactory {

    private static ClientFactory instance;

    private ClientFactory() {
    }

    static synchronized ClientFactory getInstance() {
        if(instance == null) {
            instance =  new ClientFactory();
        }
        return instance;
    }

    CMODataClient newClient(String serviceUrl, String serviceUser, String servicePassword) {
        return new CMODataClient(serviceUrl, serviceUser,  servicePassword);
    }
}
