package sap.prd.cmintegration.cli;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;
import com.sap.cmclient.http.UnexpectedHttpResponseException;

abstract class TransportRelatedABAP extends TransportRelated {

    final static private Logger logger = LoggerFactory.getLogger(TransportRelatedABAP.class);

    protected TransportRelatedABAP(BackendType type, String host, String user, String password,
            String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }


    protected Optional<Transport> getTransport() {
        com.sap.cmclient.dto.Transport transport;
        try {
            transport = AbapClientFactory.getInstance().newClient(host, user, password).getTransport(transportId);
            return transport != null ? Optional.of(transport) : Optional.empty();
        } catch (EntityProviderException | EdmException | UnexpectedHttpResponseException | IOException
                | URISyntaxException e) {
            throw new RuntimeException(String.format("Cannot retrieve transport for transportId '%s'.", transportId), e);
        }
    }
}
