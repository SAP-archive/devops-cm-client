package sap.prd.cmintegration.cli;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

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
                "Returns the owner of the transport represented by '<changeId>', '<transportId>'.");
    }
}
