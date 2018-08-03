package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the description of a transport.
 */
@CommandDescriptor(name="get-transport-description", type = BackendType.CTS)
class GetTransportDescriptionABAP extends TransportRelatedABAP {

    GetTransportDescriptionABAP(String host, String user, String password, String transportId, boolean returnCodeMode) {
        super(host, user, password, transportId, returnCodeMode);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return getDescription;
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedABAP.main(GetTransportDescriptionABAP.class, new Options(), args,
                getCommandName(GetTransportDescriptionABAP.class),
                "Returns the description for the given transport.", "");
    }

}
