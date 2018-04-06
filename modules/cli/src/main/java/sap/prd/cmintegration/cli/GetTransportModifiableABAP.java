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
@CommandDescriptor(name="is-transport-modifiable", type = BackendType.ABAP)
class GetTransportModifiableABAP extends TransportRelatedABAP {

    final static private Logger logger = LoggerFactory.getLogger(GetTransportModifiableABAP.class);

    GetTransportModifiableABAP(String host, String user, String password, String transportId) {
        super(host, user, password, transportId);
    }

    protected Function<Transport, String> getAction() {
        return isModifiable;
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", GetTransportModifiableABAP.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

        Options options = new Options();

        TransportRelatedSOLMAN.main(GetTransportModifiableABAP.class, options, args,
            format("%s [-cID <changeId>,] -tID <transportId>", getCommandName(GetTransportModifiableABAP.class)),
            "ChangeId must not be provided for ABAP backends. .Returns 'true' if the transport is modifiable. Otherwise 'false'.");
    }
}
