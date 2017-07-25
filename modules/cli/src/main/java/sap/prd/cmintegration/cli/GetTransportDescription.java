package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

@CommandDescriptor(name="get-transport-description")
class GetTransportDescription extends TransportRelated {

    GetTransportDescription(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    @Override
    protected Predicate<CMODataTransport> getOutputPredicate() {
        return it -> { String description = it.getDescription();
                       if(StringUtils.isBlank(description)) {
                           return false;
                       } else {
                           System.out.println(description); return true;}
                       };
    }

    public final static void main(String[] args) throws Exception {
        TransportRelated.main(GetTransportDescription.class, args,
                format("%s <changeId> <transportId>", getCommandName(GetTransportDescription.class)),
                "Returns the description for the transport represented by <changeId>, <transportId>.");
    }
}
