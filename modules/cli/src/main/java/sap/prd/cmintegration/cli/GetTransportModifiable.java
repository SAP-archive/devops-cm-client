package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

/**
 *  Checks if a transport is modifiable.
 */
@CommandDescriptor(name="is-transport-modifiable")
class GetTransportModifiable extends TransportRelated {

    final static private Logger logger = LoggerFactory.getLogger(GetTransportModifiable.class);
    GetTransportModifiable(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    /**
     * @return A predicate returning <code>true</code> in same something was written to <code>stdout</code>.
     * Otherwise <code>false</code>.
     */
    protected Predicate<CMODataTransport> getOutputPredicate() {
        return it -> { System.out.println(it.isModifiable()); 
                       return true;};
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", GetTransportModifiable.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        TransportRelated.main(GetTransportModifiable.class, args,
            format("%s <changeId>, <transportId>", getCommandName(GetTransportModifiable.class)),
            "Returns 'true' if the transport is modifiable. Otherwise 'false'.");
    }
}
