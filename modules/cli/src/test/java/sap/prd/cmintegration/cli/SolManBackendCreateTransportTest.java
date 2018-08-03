package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class SolManBackendCreateTransportTest extends CMSolmanTestBase {

    private Capture<String> owner, description,
                            developmentSystemId;

    @Before
    public void setup() throws Exception {
        super.setup();
        owner = Capture.newInstance();
        description = Capture.newInstance();
        developmentSystemId = Capture.newInstance();
    }

    @After
    public void tearDown() throws Exception {
        owner = null;
        description = null;
        super.tearDown();
    }

    @Test
    public void testStraightForward() throws Exception {

        setMock(setupStraightForwardMock());

        Commands.main(new String[] {
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "-t", "SOLMAN",
                "create-transport",
                "-cID", "8000038673",
                "-dID", "J01~JAVA"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(developmentSystemId.getValue(), is(equalTo("J01~JAVA")));
        assertThat(owner.hasCaptured(), is(equalTo(false)));
        assertThat(description.hasCaptured(), is(equalTo(false)));

        assertThat(IOUtils.toString(result.toByteArray(), "UTF-8").replaceAll("\\r?\\n", ""),
            is(equalTo("myTransport")));
    }

    @Test
    public void testStraightForwardWithOwnerAndDescription() throws Exception {

        setMock(setupStraightForwardMock("me", "lorem ipsum"));

        Commands.main(new String[] {
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "-t", "SOLMAN",
                "create-transport",
                "--owner", "me",
                "--description", "lorem ipsum",
                "-cID", "8000038673",
                "-dID", "J01~JAVA"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(owner.getValue(), is(equalTo("me")));
        assertThat(description.getValue(), is(equalTo("lorem ipsum")));

        assertThat(IOUtils.toString(result.toByteArray(), "UTF-8").replaceAll("\\r?\\n", ""),
            is(equalTo("myTransport")));
    }

    @Test
    public void testStraightForwardWithOwnerAndWithoutDescription() throws Exception {

        setMock(setupStraightForwardMock("me", "lorem ipsum"));

        Commands.main(new String[] {
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "-t", "SOLMAN",
                "create-transport",
                "--owner", "me",
                "-cID", "8000038673",
                "-dID", "J01~JAVA"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(owner.getValue(), is(equalTo("me")));
        assertThat(description.getValue(), is(equalTo("")));

        assertThat(IOUtils.toString(result.toByteArray(), "UTF-8").replaceAll("\\r?\\n", ""),
            is(equalTo("myTransport")));
    }

    @Test
    public void testStraightForwardWithoutOwnerAndWithDescription() throws Exception {

        setMock(setupStraightForwardMock("me", "lorem ipsum"));

        Commands.main(new String[] {
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "-t", "SOLMAN",
                "create-transport",
                "--description", "lorem ipsum",
                "-cID", "8000038673",
                "-dID", "J01~JAVA"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(owner.getValue(), is(equalTo(SERVICE_USER)));
        assertThat(description.getValue(), is(equalTo("lorem ipsum")));

        assertThat(IOUtils.toString(result.toByteArray(), "UTF-8").replaceAll("\\r?\\n", ""),
            is(equalTo("myTransport")));
    }

    private SolmanClientFactory setupStraightForwardMock() throws Exception {
        return setupStraightForwardMock(null, null);
    }

    private SolmanClientFactory setupStraightForwardMock(String owner, String description) throws Exception {

        CMODataTransport transport = new CMODataTransport("myTransport", "J01~JAVA", true, description, owner);

        CMODataSolmanClient clientMock = createMock(CMODataSolmanClient.class);
        if(owner != null && description != null) {
            expect(clientMock.createDevelopmentTransportAdvanced(
                capture(this.changeId), capture(this.developmentSystemId), capture(this.description), capture(this.owner))).andReturn(transport);
        } else {
            expect(clientMock.createDevelopmentTransport(capture(this.changeId), capture(this.developmentSystemId))).andReturn(transport);
        }

        clientMock.close(); expectLastCall();
        SolmanClientFactory factoryMock = createMock(SolmanClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);

        replay(clientMock, factoryMock);
        return factoryMock;
    }
}
