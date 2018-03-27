package sap.prd.cmintegration.cli;

import java.util.Optional;

import org.apache.commons.cli.Option;

import com.sap.cmclient.Transport;

public abstract class TransportRelated extends Command {

    protected static class Opts {
        protected final static Option TRANSPORT_ID = new Option("tID", "transport-id", true, "transportID");
    }

    protected final String changeId, transportId;

    protected TransportRelated(String host, String user, String password, String changeId, String transportId) {
        super(null, host, user, password);
        this.changeId = changeId;
        this.transportId = transportId;
    }

    protected abstract Optional<Transport> getTransport();

}
