package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;

import com.sap.cmclient.Transport;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class SolManBackendCMTransportTestBase extends CMSolmanTestBase {

    @Before
    public void setup() throws Exception {
        super.setup();;
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected SolmanClientFactory setupMock(String transportId, String owner, String description, boolean isModifiable) throws Exception {
        return setupMock(transportId, owner, description, isModifiable, null);
    }

    protected SolmanClientFactory setupMock(Exception e) throws Exception {
        return setupMock(null, null, null, false, e);
    }

    private SolmanClientFactory setupMock(String transportId, String owner, String description, boolean isModifiable, Exception ex) throws Exception {
        CMODataSolmanClient clientMock = createMock(CMODataSolmanClient.class);
        clientMock.close(); expectLastCall();
        if(ex == null) {
            ArrayList<Transport> transports = new ArrayList<>();
            transports.add(new CMODataTransport(transportId, isModifiable, description, owner));
            expect(clientMock.getChangeTransports(capture(changeId))).andReturn(transports);
        } else {
            expect(clientMock.getChangeTransports(capture(changeId))).andThrow(ex);
        }
        SolmanClientFactory factoryMock = createMock(SolmanClientFactory.class);
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

