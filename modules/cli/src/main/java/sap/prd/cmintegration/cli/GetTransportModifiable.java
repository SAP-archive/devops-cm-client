package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Predicate;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

@CommandDescriptor(name="is-transport-modifiable")
class GetTransportModifiable extends TransportRelated {

    final static String SUBCOMMAND_NAME = "is-transport-modifiable";
    
    GetTransportModifiable(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password, changeId, transportId);
    }

    /**
     * @return A predicate returning <code>true</code> in same something was written to <code>stdout</code>.
     * <Otherwise <code>false</code>.
     */
    protected Predicate<CMODataTransport> getOutputPredicate() {
        return it -> { System.out.println(it.isModifiable()); return true;};
    }

    public final static void main(String[] args) throws Exception {
        TransportRelated.main(GetTransportModifiable.class, args,
            format("%s <changeId>, <transportId>", getCommandName(GetTransportModifiable.class)),
            "Returns 'true' if the transport is modifiable. Otherwise 'false'.");
    }
}
