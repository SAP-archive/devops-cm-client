package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Predicate;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the owner of a transport.
 */
@CommandDescriptor(name="get-transport-owner", type = BackendType.SOLMAN)
class GetTransportOwnerSOLMAN extends TransportRelatedSOLMAN {

    final static private Logger logger = LoggerFactory.getLogger(GetTransportOwnerSOLMAN.class);

    GetTransportOwnerSOLMAN(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    @Override
    protected Predicate<Transport> getOutputPredicate() {
        return getOwner;
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", GetTransportOwnerSOLMAN.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

        Options options = new Options();

        TransportRelatedSOLMAN.main(GetTransportOwnerSOLMAN.class, options, args,
                format("%s [-cID <changeId>] -tID <transportId>", getCommandName(GetTransportOwnerSOLMAN.class)),
                "Returns the owner of the transport represented by [<changeId>,] <transportId>. ChangeId must not be provided for ABAP backends.");
    }
}
