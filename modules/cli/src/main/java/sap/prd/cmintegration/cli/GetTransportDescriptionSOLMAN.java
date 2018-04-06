package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the description of a transport.
 */
@CommandDescriptor(name="get-transport-description", type = BackendType.SOLMAN)
class GetTransportDescriptionSOLMAN extends TransportRelatedSOLMAN {

    final static private Logger logger = LoggerFactory.getLogger(GetTransportDescriptionSOLMAN.class);

    GetTransportDescriptionSOLMAN(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return description;
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", GetTransportDescriptionSOLMAN.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

        Options options = new Options();
        options.addOption(Commands.CMOptions.CHANGE_ID);

        TransportRelatedSOLMAN.main(GetTransportDescriptionSOLMAN.class, options, args,
                format("%s [-cID <changeId>]  -tID <transportId>", getCommandName(GetTransportDescriptionSOLMAN.class)),
                "Returns the description for the transport represented by <changeId>, <transportId>. ChangeId must not be provided for ABAP backends.");
    }

}
