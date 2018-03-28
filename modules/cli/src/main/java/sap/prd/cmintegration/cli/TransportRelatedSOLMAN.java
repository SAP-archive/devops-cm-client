package sap.prd.cmintegration.cli;

import java.util.Optional;

import com.sap.cmclient.Transport;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

abstract class TransportRelatedSOLMAN extends TransportRelated {

    protected TransportRelatedSOLMAN(String host, String user, String password,
            String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    protected Optional<Transport> getTransport() {
        try(CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(host, user, password)) {
            return client.getChangeTransports(changeId)
                       .stream()
                       .filter( it -> it.getTransportID().equals(transportId) )
                       .findFirst();
        }
    }
}
