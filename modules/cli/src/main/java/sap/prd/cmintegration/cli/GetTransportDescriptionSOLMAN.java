package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the description of a transport.
 */
@CommandDescriptor(name="get-transport-description", type = BackendType.SOLMAN)
class GetTransportDescriptionSOLMAN extends TransportRelatedSOLMAN {

    GetTransportDescriptionSOLMAN(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return description;
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedSOLMAN.main(GetTransportDescriptionSOLMAN.class, new Options(), args,
                format("%s [-cID <changeId>]  -tID <transportId>", getCommandName(GetTransportDescriptionSOLMAN.class)),
                "Returns the description for the transport represented by <changeId>, <transportId>. ChangeId must not be provided for ABAP backends.");
    }

}
