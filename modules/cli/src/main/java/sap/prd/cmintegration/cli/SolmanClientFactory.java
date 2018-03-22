package sap.prd.cmintegration.cli;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

/**
 * Provides {@link CMODataSolmanClient} instances.
 */
class SolmanClientFactory {

    private static SolmanClientFactory instance;

    private SolmanClientFactory() {
    }

    static synchronized SolmanClientFactory getInstance() {
        if(instance == null) {
            instance =  new SolmanClientFactory();
        }
        return instance;
    }

    /**
     * Provides a new instance of {@link CMODataSolmanClient}
     * @param serviceUrl The OData endpoint of the SAP Solution Manager
     * @param serviceUser The service user.
     * @param servicePassword The password for authenticating.
     * @return A new instance of {@link CMODataSolmanClient}
     */
    CMODataSolmanClient newClient(String serviceUrl, String serviceUser, String servicePassword) {
        return new CMODataSolmanClient(serviceUrl, serviceUser,  servicePassword);
    }
}
