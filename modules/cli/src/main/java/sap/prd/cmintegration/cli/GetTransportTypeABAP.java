package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the owner of a transport.
 */
@CommandDescriptor(name="get-transport-type", type = BackendType.ABAP)
class GetTransportTypeABAP extends TransportRelatedABAP {

    GetTransportTypeABAP(String host, String user, String password, String transportId, boolean returnCodeMode) {
        super(host, user, password, transportId, returnCodeMode);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return new Function<Transport, String>() {

            @Override
            public String apply(Transport t) {

                // ... ugly downcast
                String type = ((com.sap.cmclient.dto.Transport)t).getType();
                return (StringUtils.isBlank(type)) ? null : type;
            }
        };
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedABAP.main(GetTransportTypeABAP.class, new Options(), args,
                getCommandName(GetTransportTypeABAP.class), "",
                "Returns the type of the given transport");
    }
}
