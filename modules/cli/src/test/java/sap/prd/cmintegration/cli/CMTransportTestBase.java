package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class CMTransportTestBase extends CMTestBase {

    @Before
    public void setup() throws Exception {
        super.setup();;
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected ClientFactory setupMock(String transportId, String owner, String description, boolean isModifiable) throws Exception {
        return setupMock(transportId, owner, description, isModifiable, null);
    }

    protected ClientFactory setupMock(String transportId, String owner, String description, boolean isModifiable, Exception ex) throws Exception {
        CMODataClient clientMock = createMock(CMODataClient.class);
        if(ex == null) {
            ArrayList<CMODataTransport> transports = new ArrayList<>();
            transports.add(new CMODataTransport(transportId, isModifiable, description, owner));
            expect(clientMock.getChangeTransports(capture(changeId))).andReturn(transports);
        } else {
            expect(clientMock.getChangeTransports(capture(changeId))).andThrow(ex);
        }
        ClientFactory factoryMock = createMock(ClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);

        replay(clientMock, factoryMock);
        return factoryMock;
    }

    protected static String removeCRLF(String str) {
        return str.replaceAll("\\r?\\n$", "");
    }
}

