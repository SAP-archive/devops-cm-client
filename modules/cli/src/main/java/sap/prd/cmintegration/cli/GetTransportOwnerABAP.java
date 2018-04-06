package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the owner of a transport.
 */
@CommandDescriptor(name="get-transport-owner", type = BackendType.ABAP)
class GetTransportOwnerABAP extends TransportRelatedABAP {

    GetTransportOwnerABAP(String host, String user, String password, String transportId) {
        super(host, user, password, transportId);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return getOwner;
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedABAP.main(GetTransportOwnerABAP.class, new Options(), args,
                format("%s -%s <%s>", getCommandName(GetTransportOwnerABAP.class),
                Opts.TRANSPORT_ID.getOpt(), Opts.TRANSPORT_ID.getArgName()),
                format("Returns the owner of the transport represented by <%s>.", Opts.TRANSPORT_ID.getArgName()));
    }
}
