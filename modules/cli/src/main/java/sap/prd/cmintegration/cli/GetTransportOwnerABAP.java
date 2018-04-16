package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the owner of a transport.
 */
@CommandDescriptor(name="get-transport-owner", type = BackendType.CTS)
class GetTransportOwnerABAP extends TransportRelatedABAP {

    GetTransportOwnerABAP(String host, String user, String password, String transportId, boolean returnCodeMode) {
        super(host, user, password, transportId, returnCodeMode);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return getOwner;
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedABAP.main(GetTransportOwnerABAP.class, new Options().addOption(Opts.TRANSPORT_ID), args,
                getCommandName(GetTransportOwnerABAP.class),
                "Returns the owner of the given transport.", "");
    }
}
