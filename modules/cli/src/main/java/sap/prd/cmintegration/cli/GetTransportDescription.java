package sap.prd.cmintegration.cli;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

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
                "Returns the description for the transport represented by '<changeId>', '<transportId>'.");
    }
}
