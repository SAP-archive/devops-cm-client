package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

@CommandDescriptor(name="get-transport-description")
class GetTransportDescription extends TransportRelated {

    final static private Logger logger = LoggerFactory.getLogger(GetTransportDescription.class);
    GetTransportDescription(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    @Override
    protected Predicate<CMODataTransport> getOutputPredicate() {
        return it -> { String description = it.getDescription();
                       if(StringUtils.isBlank(description)) {
                           logger.debug(String.format("Tansport Id: '%s' Owner: '%s' isModifiable: '%s'", it.getTransportID(), it.getOwner(), Boolean.toString(it.isModifiable())));
                           return false;
                       } else {
                           System.out.println(description); 
                           logger.debug(String.format("Tansport Id: '%s' Owner: '%s' Description: '%s' isModifiable: '%s'", it.getTransportID(), it.getOwner(),it.getDescription() , Boolean.toString(it.isModifiable())));
                           return true;}
                       };
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(Commands.Helpers.getArgsLogString(args));
        TransportRelated.main(GetTransportDescription.class, args,
                format("%s <changeId> <transportId>", getCommandName(GetTransportDescription.class)),
                "Returns the description for the transport represented by <changeId>, <transportId>.");
    }
}
