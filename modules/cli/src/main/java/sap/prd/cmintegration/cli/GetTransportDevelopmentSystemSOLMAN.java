package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import com.sap.cmclient.Transport;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

/**
 * Command for retrieving the description of a transport.
 */
@CommandDescriptor(name="get-transport-development-system", type = BackendType.SOLMAN)
class GetTransportDevelopmentSystemSOLMAN extends TransportRelatedSOLMAN {

    GetTransportDevelopmentSystemSOLMAN(String host, String user, String password, String changeId, String transportId, boolean isReturnCodeMode) {
        super(host, user, password, changeId, transportId, isReturnCodeMode);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return new Function<Transport, String>() {

            @Override
            public String apply(Transport t) {
                String developmentSystem = ((CMODataTransport)t).getDevelopmentSystemID();
                if(StringUtils.isBlank(developmentSystem)) {
                    developmentSystem = "<n/a>";
                }
                return developmentSystem;
            };
        };
    }

    public final static void main(String[] args) throws Exception {
        TransportRelatedSOLMAN.main(GetTransportDevelopmentSystemSOLMAN.class, new Options(), args,
                getCommandName(GetTransportDevelopmentSystemSOLMAN.class),
                "Returns the development system id for the given transport.", "");
    }

}
