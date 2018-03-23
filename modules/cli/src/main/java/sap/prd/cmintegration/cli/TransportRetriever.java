package sap.prd.cmintegration.cli;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;

import com.sap.cmclient.Transport;
import com.sap.cmclient.http.CMODataAbapClient;
import com.sap.cmclient.http.UnexpectedHttpResponseException;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

/**
 * Unifies getting transports. Abstracts away the differences between getting a
 * transport from SAP-SolutionManager and from ABAP systems.
 *
 */
public abstract class TransportRetriever {

    enum BackendType {ABAP, SOLMAN};

    protected final String endpoint, 
                         user,
                         password;

    TransportRetriever(String endpoint, String user, String password) {
        this.endpoint = endpoint;
        this.user = user;
        this.password = password;
    }

    public static TransportRetriever get(BackendType type, String endpoint, String user, String password) {
        switch (type) {
        case ABAP:
            return new AbapTransportRetriever(endpoint, user, password);
        case SOLMAN:
            return new SolmanTransportRetriever(endpoint, user, password);
        default:
            throw new IllegalArgumentException(String.format("Invalid backendType '%s'.", type));
        }
    }

    abstract Optional<Transport> getTransport(BackendType type, String changeId, String transportId);
}

class AbapTransportRetriever extends TransportRetriever {

    AbapTransportRetriever(String endpoint, String user, String password) {
        super(endpoint, user, password);
    }

    /**
     * ChangeId is only present in order to fullfill the method contract from the pqrent class. Actually
     * for ABAP systems there is nothing like a changeId.
     */
    @Override
    Optional<Transport> getTransport(BackendType type, String changeId, String transportId) {
        try {
            com.sap.cmclient.dto.Transport transport = new CMODataAbapClient(endpoint, user, password).getTransport(transportId);
            return transport != null ? Optional.of(transport) : Optional.empty();
        } catch (EntityProviderException | EdmException | UnexpectedHttpResponseException | IOException
              | URISyntaxException e) {
          throw new RuntimeException(String.format("Cannot retrieve transport for transportId '%s'.", transportId), e);
        }
    }
}

class SolmanTransportRetriever extends TransportRetriever {

    SolmanTransportRetriever(String endpoint, String user, String password) {
        super(endpoint, user, password);
    }

    @Override
    Optional<Transport> getTransport(BackendType type, String changeId, String transportId) {
        try(CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(endpoint, user, password)) {
            return client.getChangeTransports(changeId)
                       .stream()
                       .filter( it -> it.getTransportID().equals(transportId) )
                       .findFirst();
        }
    }
}