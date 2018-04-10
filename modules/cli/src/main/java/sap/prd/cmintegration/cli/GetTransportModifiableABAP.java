package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;

import com.sap.cmclient.Transport;

/**
 *  Checks if a transport is modifiable.
 */
@CommandDescriptor(name="is-transport-modifiable", type = BackendType.ABAP)
class GetTransportModifiableABAP extends TransportRelatedABAP {

    GetTransportModifiableABAP(String host, String user, String password, String transportId) {
        super(host, user, password, transportId);
    }

    protected Function<Transport, String> getAction() {
        return isModifiable;
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedABAP.main(GetTransportModifiableABAP.class, new Options(), args,
            getCommandName(GetTransportModifiableABAP.class), "",
            "Returns 'true' if the transport is modifiable. Otherwise 'false'.");
    }
}
