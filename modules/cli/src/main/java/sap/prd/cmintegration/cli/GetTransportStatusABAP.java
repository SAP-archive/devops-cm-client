package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the owner of a transport.
 */
@CommandDescriptor(name="get-transport-status", type = BackendType.ABAP)
class GetTransportStatusABAP extends TransportRelatedABAP {

    GetTransportStatusABAP(String host, String user, String password, String transportId) {
        super(host, user, password, transportId);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return new Function<Transport, String>() {

            @Override
            public String apply(Transport t) {

                // ... ugly downcast
                String status = ((com.sap.cmclient.dto.Transport)t).getStatus();
                return (StringUtils.isBlank(status)) ? null : status;
            }
        };
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedABAP.main(GetTransportStatusABAP.class, new Options(), args,
                getCommandName(GetTransportStatusABAP.class), "",
                "Returns the status of the given transport");
    }
}
