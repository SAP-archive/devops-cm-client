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

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

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
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "dummy-cmd",
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
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "dummy-cmd",
                "8000038673", "L21K900026"});
    }

    private SolmanClientFactory setupMock(Exception e) throws Exception {

        CMODataSolmanClient clientMock = EasyMock.createMock(CMODataSolmanClient.class);
        clientMock.releaseDevelopmentTransport(capture(changeId), capture(transportId));
        clientMock.close(); expectLastCall();
        if(e == null) expectLastCall(); else expectLastCall().andThrow(e);

        SolmanClientFactory factoryMock = EasyMock.createMock(SolmanClientFactory.class);
        expect(factoryMock
            .newClient(capture(host),
                    capture(user),
                    capture(password))).andReturn(clientMock);

        EasyMock.replay(clientMock, factoryMock);
        return factoryMock;
    }
}
