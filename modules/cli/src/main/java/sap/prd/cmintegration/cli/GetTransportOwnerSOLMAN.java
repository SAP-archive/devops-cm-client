package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the owner of a transport.
 */
@CommandDescriptor(name="get-transport-owner", type = BackendType.SOLMAN)
class GetTransportOwnerSOLMAN extends TransportRelatedSOLMAN {

    GetTransportOwnerSOLMAN(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return getOwner;
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedSOLMAN.main(GetTransportOwnerSOLMAN.class, new Options(), args,
                getCommandName(GetTransportOwnerSOLMAN.class), "",
                "Returns the owner of the given transport.");
    }
}
