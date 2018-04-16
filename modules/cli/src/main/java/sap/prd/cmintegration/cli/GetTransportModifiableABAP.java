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

    static class Opts {

        static Options addOption(Options options, boolean includeStandardOpts) {
            TransportRelated.Opts.addOpts(options, includeStandardOpts);
            return options.addOption(Commands.CMOptions.RETURN_CODE);
        }
    }

    GetTransportModifiableABAP(String host, String user, String password, String transportId, boolean returnCodeMode) {
        super(host, user, password, transportId, returnCodeMode);
    }

    protected Function<Transport, String> getAction() {
        return isModifiable;
    }

    public final static void main(String[] args) throws Exception {

        Options opts = Opts.addOption(new Options(), false); // will be added later in the parent class

        TransportRelatedABAP.main(GetTransportModifiableABAP.class, opts, args,
            getCommandName(GetTransportModifiableABAP.class), "",
            "Returns 'true' if the transport is modifiable. Otherwise 'false'.");
    }
}
