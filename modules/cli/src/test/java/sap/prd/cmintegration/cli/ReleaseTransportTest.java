package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class ReleaseTransportTest extends CMTestBase {

    private Capture<String> transportId = null;

    @Before
    public void setup() throws Exception {
        super.setup();
        transportId = Capture.newInstance();
    }

    @After
    public void tearDown() throws Exception {
        transportId = null;
        super.tearDown();
    }

    @Test
    public void testReleaseTransportStraightForward() throws Exception {

        // comment line below in order to run against real back-end
        setMock(setupMock(null));

        ReleaseTransport.main(new String[] {
                "-u", "john.doe",
                "-p", "openSesame",
                "-h", "https://example.org/endpoint/",
                "8000038673", "L21K90002K"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(transportId.getValue(), is(equalTo("L21K90002K")));
    }

    @Test
    public void testReleaseTransportFailsSinceTransportHasAlreadyBeenReleased() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("400");

        // comment line below in order to run against real back-end
        setMock(setupMock(new ODataClientErrorException(StatusLines.BAD_REQUEST)));

        ReleaseTransport.main(new String[] {
                "-u", "john.doe",
                "-p", "openSesame",
                "-h", "https://example.org/endpoint/",
                "8000038673", "L21K900026"});
    }

    private ClientFactory setupMock(Exception e) throws Exception {

        CMODataClient clientMock = EasyMock.createMock(CMODataClient.class);
        clientMock.releaseDevelopmentTransport(capture(changeId), capture(transportId));
        clientMock.close(); expectLastCall();
        if(e == null) expectLastCall(); else expectLastCall().andThrow(e);

        ClientFactory factoryMock = EasyMock.createMock(ClientFactory.class);
        expect(factoryMock
            .newClient(capture(host),
                    capture(user),
                    capture(password))).andReturn(clientMock);

        EasyMock.replay(clientMock, factoryMock);
        return factoryMock;
    }
}
