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
import org.apache.olingo.client.api.communication.ODataClientErrorException;
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
    public void testGetChangeStatusStraightForward() throws Exception {

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        GetChangeStatus.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "dummy-cmd",
        "8000038673"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(user.getValue(), is(equalTo(SERVICE_USER)));
        assertThat(password.getValue(), is(equalTo(SERVICE_PASSWORD)));
        assertThat(host.getValue(), is(equalTo(SERVICE_ENDPOINT)));

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
        "-e", SERVICE_ENDPOINT,
        "dummy-cmd",
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
            "-u", SERVICE_USER,
            "-p", SERVICE_PASSWORD,
            "-e", SERVICE_ENDPOINT,
            "dummy-cmd",
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
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT});
    }

    @Test
    public void testGetChangeStatusPasswordViaStdin() throws Exception {

        InputStream oldIn = System.in;
        System.setIn(new ByteArrayInputStream(SERVICE_PASSWORD.getBytes()));

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        try {
          GetChangeStatus.main(new String[] {
          "-u", SERVICE_USER,
          "-p", "-",
          "-e", SERVICE_ENDPOINT,
          "dummy-cmd",
          "8000038673"});
        } finally {
            System.setIn(oldIn);
        }

        assertThat(password.getValue(), is(equalTo(SERVICE_PASSWORD)));
    }

    @Test
    public void testGetChangeStatusMultilinePasswordViaStdin() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Multiline passwords are not supported.");

        InputStream oldIn = System.in;
        System.setIn(new ByteArrayInputStream(SERVICE_PASSWORD.concat("\r\nPWDAGAIN").getBytes()));

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        try {
          GetChangeStatus.main(new String[] {
          "-u", SERVICE_USER,
          "-p", "-",
          "-e", SERVICE_ENDPOINT,
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
          "-u", SERVICE_USER,
          "-p", "-",
          "-e", SERVICE_ENDPOINT,
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
        "-u", SERVICE_USER,
        "-e", SERVICE_ENDPOINT,
        "8000038673"});
    }
}
