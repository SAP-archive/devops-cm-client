package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.io.IOUtils;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.hamcrest.Matchers;
import org.junit.Test;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class GetChangeStatusTest extends CMTestBase {

    private ClientFactory setupMock() throws Exception {
        return setupMock(null);
    }

    private ClientFactory setupMock(Exception ex) throws Exception {
        CMODataClient clientMock = createMock(CMODataClient.class);
        clientMock.close(); expectLastCall();
        if(ex == null) {
            CMODataChange change = new CMODataChange("8000038673", true);
            expect(clientMock.getChange(capture(changeId))).andReturn(change);
        } else {
            expect(clientMock.getChange(capture(changeId))).andThrow(ex);
        }
        ClientFactory factoryMock = createMock(ClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);

        replay(clientMock, factoryMock);
        return factoryMock;
    }

    @Test
    public void testPrintHelp() throws Exception {
        GetChangeStatus.main(new String[] {"--help"});
        String help = IOUtils.toString(result.toByteArray(), "UTF-8");
        assertThat(help, Matchers.containsString("Prints this help."));
    }

    @Test
    public void testGetChangeStatusStraightForward() throws Exception {

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        GetChangeStatus.main(new String[] {
        "-u", "john.doe",
        "-p", "openSesame",
        "-e", "https://example.org/endpoint/",
        "8000038673"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(user.getValue(), is(equalTo("john.doe")));
        assertThat(password.getValue(), is(equalTo("openSesame")));
        assertThat(host.getValue(), is(equalTo("https://example.org/endpoint/")));

        assertThat(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.toByteArray()), "UTF-8")).readLine(), equalTo("true"));
    }

    @Test
    public void testGetChangeStatusWithBadCredentials() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("401");
        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock(new ODataClientErrorException(StatusLines.UNAUTHORIZED)));

        GetChangeStatus.main(new String[] {
        "-u", "DOES_NOT_EXIST",
        "-p", "********",
        "-e", "https://example.org/endpoint/",
        "8000038673"});
    }

    @Test
    public void testGetChangeStatusForNotExistingChange() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("404");
        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock(new ODataClientErrorException(StatusLines.NOT_FOUND)));

        try {
            GetChangeStatus.main(new String[] {
            "-u", "john.doe",
            "-p", "openSesame",
            "-e", "https://example.org/endpoint/",
            "DOES_NOT_EXIST"});
        } catch(Exception e) {
            assertThat(changeId.getValue(), is(equalTo("DOES_NOT_EXIST")));
            throw e;
        }
    }

    @Test
    public void testGetChangeStatusWithoutChangeId() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("No changeId specified.");
        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        GetChangeStatus.main(new String[] {
        "-u", "john.doe",
        "-p", "openSesame",
        "-e", "https://example.org/endpoint/"});
    }

    @Test
    public void testGetChangeStatusPasswordViaStdin() throws Exception {

        InputStream oldIn = System.in;
        System.setIn(new ByteArrayInputStream("openSesame".getBytes()));

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        try {
          GetChangeStatus.main(new String[] {
          "-u", "john.doe",
          "-p", "-",
          "-e", "https://example.org/endpoint/",
          "8000038673"});
        } finally {
            System.setIn(oldIn);
        }

        assertThat(password.getValue(), is(equalTo("openSesame")));
    }

    @Test
    public void testGetChangeStatusMultilinePasswordViaStdin() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Multiline passwords are not supported.");

        InputStream oldIn = System.in;
        System.setIn(new ByteArrayInputStream("openSesame\r\nTESTAGAIN".getBytes()));

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        try {
          GetChangeStatus.main(new String[] {
          "-u", "john.doe",
          "-p", "-",
          "-e", "https://example.org/endpoint/",
          "8000038673"});
        } finally {
            System.setIn(oldIn);
        }
    }
    @Test
    public void testGetChangeStatusEmptyPasswordViaStdin() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Empty password found.");

        InputStream oldIn = System.in;
        System.setIn(new ByteArrayInputStream("".getBytes()));

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        try {
          GetChangeStatus.main(new String[] {
          "-u", "john.doe",
          "-p", "-",
          "-e", "https://example.org/endpoint/",
          "8000038673"});
        } finally {
            System.setIn(oldIn);
        }
    }

    @Test
    public void testGetChangeStatusNoPassword() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: p");

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        GetChangeStatus.main(new String[] {
        "-u", "john.doe",
        "-e", "https://example.org/endpoint/",
        "8000038673"});
    }
}
