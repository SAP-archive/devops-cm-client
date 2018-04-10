package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;

import com.sap.cmclient.Transport;

/**
 *  Checks if a transport is modifiable.
 */
@CommandDescriptor(name="is-transport-modifiable", type = BackendType.SOLMAN)
class GetTransportModifiableSOLMAN extends TransportRelatedSOLMAN {

    GetTransportModifiableSOLMAN(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    protected Function<Transport, String> getAction() {
        return isModifiable;
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedSOLMAN.main(GetTransportModifiableSOLMAN.class, new Options(), args,
            getCommandName(GetTransportModifiableSOLMAN.class), "",
            "Returns 'true' if the transport is modifiable. Otherwise 'false'.");
    }
}
