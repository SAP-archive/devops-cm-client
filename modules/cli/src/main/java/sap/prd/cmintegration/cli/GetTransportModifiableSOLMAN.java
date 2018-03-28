package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;

/**
 *  Checks if a transport is modifiable.
 */
@CommandDescriptor(name="is-transport-modifiable", type = BackendType.SOLMAN)
class GetTransportModifiableSOLMAN extends TransportRelatedSOLMAN {

    final static private Logger logger = LoggerFactory.getLogger(GetTransportModifiableSOLMAN.class);

    GetTransportModifiableSOLMAN(BackendType backendType, String host, String user, String password, String changeId, String transportId) {
        super(backendType, host, user, password, changeId, transportId);
    }

    protected Predicate<Transport> getOutputPredicate() {
        return isModifiable;
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", GetTransportModifiableSOLMAN.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        TransportRelatedSOLMAN.main(GetTransportModifiableSOLMAN.class, args,
            format("%s [-cID <changeId>,] -tID <transportId>", getCommandName(GetTransportModifiableSOLMAN.class)),
            "ChangeId must not be provided for ABAP backends. .Returns 'true' if the transport is modifiable. Otherwise 'false'.");
    }
}
