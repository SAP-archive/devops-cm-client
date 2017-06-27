package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.io.IOUtils;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.easymock.EasyMock;
import org.hamcrest.Matchers;
import org.junit.Test;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class GetChangeStatusTest extends CMTestBase {

    private ClientFactory setupMock() throws Exception {
        return setupMock(null);
    }

    private ClientFactory setupMock(Exception ex) throws Exception {
        CMODataClient clientMock = EasyMock.createMock(CMODataClient.class);
        if(ex == null) {
            CMODataChange change = new CMODataChange("8000038673", "E0002");
            expect(clientMock.getChange(capture(changeId))).andReturn(change);
        } else {
            expect(clientMock.getChange(capture(changeId))).andThrow(ex);
        }
        ClientFactory factoryMock = EasyMock.createMock(ClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);

        EasyMock.replay(clientMock, factoryMock);
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
        "-h", "https://example.org/endpoint/",
        "8000038673"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(user.getValue(), is(equalTo("john.doe")));
        assertThat(password.getValue(), is(equalTo("openSesame")));
        assertThat(host.getValue(), is(equalTo("https://example.org/endpoint/")));

        assertThat(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.toByteArray()), "UTF-8")).readLine(), equalTo("E0002"));
    }

    @Test
    public void testGetChangeStatusWithBadCredentials() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("401");
        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(
                setupMock(
                    new ODataClientErrorException(
                        new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 401, "Unauthorized"))));

        GetChangeStatus.main(new String[] {
        "-u", "DOES_NOT_EXIST",
        "-p", "********",
        "-h", "https://example.org/endpoint/",
        "8000038673"});
    }

    @Test
    public void testGetChangeStatusForNotExistingChange() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("400");
        //
        // Comment statement below in order to go against the real back-end as specified via -h
        setMock(
            setupMock(
                new ODataClientErrorException(
                    new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 400, "Bad Request"))));

        try {
            GetChangeStatus.main(new String[] {
            "-u", "john.doe",
            "-p", "openSesame",
            "-h", "https://example.org/endpoint/",
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
        "-h", "https://example.org/endpoint/"});
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
          "-h", "https://example.org/endpoint/",
          "8000038673"});
        } finally {
            System.setIn(oldIn);
        }

        assertThat(password.getValue(), is(equalTo("openSesame")));
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
        "-h", "https://example.org/endpoint/",
        "8000038673"});
    }
}
