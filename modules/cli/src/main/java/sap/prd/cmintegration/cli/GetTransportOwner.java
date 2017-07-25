package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

@CommandDescriptor(name="get-transport-owner")
class GetTransportOwner extends TransportRelated {

    GetTransportOwner(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    @Override
    protected Predicate<CMODataTransport> getOutputPredicate() {
        return it -> { String owner = it.getOwner();
                       if(StringUtils.isBlank(owner)) {
                           return false;
                       } else {
                           System.out.println(owner); return true;}
                       };
    }

    public final static void main(String[] args) throws Exception {
        TransportRelated.main(GetTransportOwner.class, args,
                format("%s <changeId> <transportId>", getCommandName(GetTransportOwner.class)),
                "Returns the owner of the transport represented by <changeId>, <transportId>.");
    }
}
