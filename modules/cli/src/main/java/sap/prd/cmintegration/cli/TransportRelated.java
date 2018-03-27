package sap.prd.cmintegration.cli;

import com.google.common.base.Optional;
import com.sap.cmclient.Transport;

public abstract class TransportRelated extends Command {

    protected final String changeId, transportId;

    protected TransportRelated(String host, String user, String password, String changeId, String transportId) {
        super(null, host, user, password);
        this.changeId = changeId;
        this.transportId = transportId;
    }

    protected abstract Optional<Transport> getTransport();

}
