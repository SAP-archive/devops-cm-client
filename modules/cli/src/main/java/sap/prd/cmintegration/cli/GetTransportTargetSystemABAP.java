package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the owner of a transport.
 */
@CommandDescriptor(name="get-transport-target-system", type = BackendType.ABAP)
class GetTransportTargetSystemABAP extends TransportRelatedABAP {

    GetTransportTargetSystemABAP(String host, String user, String password, String transportId, boolean returnCodeMode) {
        super(host, user, password, transportId, returnCodeMode);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return new Function<Transport, String>() {

            @Override
            public String apply(Transport t) {

                // ... ugly downcast
                String status = ((com.sap.cmclient.dto.Transport)t).getTargetSystem();
                return (StringUtils.isBlank(status)) ? null : status;
            }
        };
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedABAP.main(GetTransportTargetSystemABAP.class, new Options(), args,
                getCommandName(GetTransportTargetSystemABAP.class), "",
                "Returns the status of the given transport.");
    }
}
