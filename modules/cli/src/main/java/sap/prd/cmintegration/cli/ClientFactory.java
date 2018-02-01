package sap.prd.cmintegration.cli;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

/**
 * Provides {@link CMODataClient} instances.
 */
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

    /**
     * Provides a new instance of {@link CMODataClient}
     * @param serviceUrl The OData endpoint of the SAP Solution Manager
     * @param serviceUser The service user.
     * @param servicePassword The password for authenticating.
     * @return A new instance of {@link CMODataClient}
     */
    CMODataClient newClient(String serviceUrl, String serviceUser, String servicePassword) {
        return new CMODataClient(serviceUrl, serviceUser,  servicePassword);
    }
}
