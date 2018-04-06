package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;

/**
 *  Checks if a transport is modifiable.
 */
@CommandDescriptor(name="is-transport-modifiable", type = BackendType.SOLMAN)
class GetTransportModifiableSOLMAN extends TransportRelatedSOLMAN {

    final static private Logger logger = LoggerFactory.getLogger(GetTransportModifiableSOLMAN.class);

    GetTransportModifiableSOLMAN(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    protected Function<Transport, String> getAction() {
        return isModifiable;
    }

    public final static void main(String[] args) throws Exception {
        Options opts = new Options();
        logger.debug(format("%s called with arguments: '%s'.", GetTransportModifiableSOLMAN.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        TransportRelatedSOLMAN.main(GetTransportModifiableSOLMAN.class, opts, args,
            format("%s [-cID <changeId>,] -tID <transportId>", getCommandName(GetTransportModifiableSOLMAN.class)),
            "ChangeId must not be provided for ABAP backends. .Returns 'true' if the transport is modifiable. Otherwise 'false'.");
    }
}
