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
        return it -> {
                       String description = it.getDescription();
                       if(StringUtils.isBlank(description)) {
                           logger.debug(format("Description of transport '%s' is blank. Nothing will be emitted.", it.getTransportID()));
                           return false;
                       } else {
                           logger.debug(format("Description of transport '%s' is not blank. Description '%s' will be emitted.", it.getTransportID(), it.getDescription()));
                           System.out.println(description); 
                           return true;}
                       };
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", GetTransportDescription.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        TransportRelated.main(GetTransportDescription.class, args,
                format("%s <changeId> <transportId>", getCommandName(GetTransportDescription.class)),
                "Returns the description for the transport represented by <changeId>, <transportId>.");
    }
}
