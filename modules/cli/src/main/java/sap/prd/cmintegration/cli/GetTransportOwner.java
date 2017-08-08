package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

@CommandDescriptor(name="get-transport-owner")
class GetTransportOwner extends TransportRelated {

    final static private Logger logger = LoggerFactory.getLogger(GetTransportOwner.class);
    GetTransportOwner(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    @Override
    protected Predicate<CMODataTransport> getOutputPredicate() {
        return it -> { String owner = it.getOwner();
                       if(StringUtils.isBlank(owner)) {
                           logger.debug(String.format("Owner attribute for transport '%s' is blank. Nothing will be emitted.", it.getTransportID()));
                           return false;
                       } else {
                           System.out.println(owner); 
                           logger.debug(String.format("Owner '%s' has been emitted for transport '%s'.", it.getOwner(), it.getTransportID()));
                           return true;}
                       };
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", GetTransportOwner.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        TransportRelated.main(GetTransportOwner.class, args,
                format("%s <changeId> <transportId>", getCommandName(GetTransportOwner.class)),
                "Returns the owner of the transport represented by <changeId>, <transportId>.");
    }
}
