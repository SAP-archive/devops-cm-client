package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class CreateTransportTest extends CMTestBase {

    @Before
    public void setup() throws Exception {
        super.setup();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testStraightForward() throws Exception {

        setMock(setupStraightForwardMock());

        CreateTransport.main(new String[] {
                "-u", "john.doe",
                "-p", "openSesame",
                "-h", "https://example.org/endpoint/",
                "8000038673"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));

        assertThat(IOUtils.toString(result.toByteArray(), "UTF-8").replaceAll("\\r?\\n", ""),
            is(equalTo("myTransport")));
    }

    private ClientFactory setupStraightForwardMock() throws Exception {

        CMODataTransport transport = new CMODataTransport("myTransport", true, "Lorum ipsum", "me");

        CMODataClient clientMock = EasyMock.createMock(CMODataClient.class);
        expect(clientMock.createDevelopmentTransport(capture(changeId))).andReturn(transport);
        ClientFactory factoryMock = EasyMock.createMock(ClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);

        EasyMock.replay(clientMock, factoryMock);
        return factoryMock;
    }
}
