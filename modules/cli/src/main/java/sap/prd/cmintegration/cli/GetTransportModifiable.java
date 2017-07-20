package sap.prd.cmintegration.cli;

import java.util.function.Predicate;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class GetTransportModifiable extends TransportRelated {

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
        TransportRelated.main(GetTransportModifiable.class, args);
    }
}
