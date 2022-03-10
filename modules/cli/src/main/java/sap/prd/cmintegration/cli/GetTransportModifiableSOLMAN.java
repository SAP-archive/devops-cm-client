package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;

import com.sap.cmclient.Transport;

/**
 *  Checks if a transport is modifiable.
 */
@CommandDescriptor(name="is-transport-modifiable")
class GetTransportModifiableSOLMAN extends TransportRelatedSOLMAN {

    private static class Opts {
        static Options addOptions(Options opts, boolean includeStandardOpts) {
            TransportRelatedSOLMAN.Opts.addOptions(opts, includeStandardOpts);
            return opts.addOption(Commands.CMOptions.RETURN_CODE);
        }
    }
    GetTransportModifiableSOLMAN(String host, String user, String password, String changeId, String transportId, boolean returnCodeMode) {
        super(host, user, password, changeId, transportId, returnCodeMode);
    }

    protected Function<Transport, String> getAction() {
        return isModifiable;
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedSOLMAN.main(GetTransportModifiableSOLMAN.class, Opts.addOptions(new Options(), true), args,
            getCommandName(GetTransportModifiableSOLMAN.class), "",
            "Returns 'true' if the transport is modifiable. Otherwise 'false'.");
    }
}
