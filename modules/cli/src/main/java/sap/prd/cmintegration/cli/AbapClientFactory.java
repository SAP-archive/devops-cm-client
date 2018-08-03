package sap.prd.cmintegration.cli;

import java.net.URISyntaxException;

import com.sap.cmclient.http.CMODataAbapClient;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

public class AbapClientFactory {

    private static AbapClientFactory instance;

    private AbapClientFactory() {

    }

    static synchronized AbapClientFactory getInstance() {
        if(instance == null) {
            instance =  new AbapClientFactory();
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
    CMODataAbapClient newClient(String serviceUrl, String serviceUser, String servicePassword) throws URISyntaxException {
        return new CMODataAbapClient(serviceUrl, serviceUser, servicePassword);
    }
}
