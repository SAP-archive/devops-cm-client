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
import org.junit.Test;

import com.sap.cmclient.Matchers;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

public class SolManBackendGetChangeStatusTest extends CMSolmanTestBase {

    private SolmanClientFactory setupMock() throws Exception {
        return setupMock(true, null);
    }

    private SolmanClientFactory setupMock(boolean isInDevelopment) throws Exception {
        return setupMock(isInDevelopment, null);
    }

    private SolmanClientFactory setupMock(Exception ex) throws Exception {
        return setupMock(true, ex);
    }

    private SolmanClientFactory setupMock(boolean isInDevelopment, Exception ex) throws Exception {
        CMODataSolmanClient clientMock = createMock(CMODataSolmanClient.class);
        clientMock.close(); expectLastCall();
        if(ex == null) {
            CMODataChange change = new CMODataChange("8000038673", isInDevelopment);
            expect(clientMock.getChange(capture(changeId))).andReturn(change);
        } else {
            expect(clientMock.getChange(capture(changeId))).andThrow(ex);
        }
        SolmanClientFactory factoryMock = createMock(SolmanClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);

        replay(clientMock, factoryMock);
        return factoryMock;
    }

    @Test
    public void testGetChangeStatusStraightForwardViaStdout() throws Exception {

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        Commands.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "-t", "SOLMAN",
        "is-change-in-development",
        "-cID", "8000038673"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(user.getValue(), is(equalTo(SERVICE_USER)));
        assertThat(password.getValue(), is(equalTo(SERVICE_PASSWORD)));
        assertThat(host.getValue(), is(equalTo(SERVICE_ENDPOINT)));

        assertThat(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.toByteArray()), "UTF-8")).readLine(), equalTo("true"));
    }

    @Test
    public void testGetChangeStatusStraightForwardViaStdoutReturnsFalseWhenChangeIsNotInDevelopment() throws Exception {

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock(false));

        Commands.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "-t", "SOLMAN",
        "is-change-in-development",
        "-cID", "8000038673"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(user.getValue(), is(equalTo(SERVICE_USER)));
        assertThat(password.getValue(), is(equalTo(SERVICE_PASSWORD)));
        assertThat(host.getValue(), is(equalTo(SERVICE_ENDPOINT)));

        assertThat(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.toByteArray()), "UTF-8")).readLine(), equalTo("false"));
    }

    @Test
    public void testGetChangeStatusReturnsTrueStraightForwardViaReturnCode() throws Exception {

        // The absence of an exception means "change is in development"

        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        Commands.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "-t", "SOLMAN",
        "is-change-in-development",
        "--return-code",
        "-cID", "8000038673"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(user.getValue(), is(equalTo(SERVICE_USER)));
        assertThat(password.getValue(), is(equalTo(SERVICE_PASSWORD)));
        assertThat(host.getValue(), is(equalTo(SERVICE_ENDPOINT)));

        assertThat(IOUtils.toString(new ByteArrayInputStream(result.toByteArray()), "UTF-8"), equalTo(""));
    }

    @Test
    public void testGetChangeStatusThrowsExceptionStraightForwardViaReturnCode() throws Exception {

        // The absence of an exception means "change is in development"

        thrown.expect(ExitException.class);
        thrown.expect(sap.prd.cmintegration.cli.Matchers.exitCode(3));

        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock(false));

        Commands.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "-t", "SOLMAN",
        "is-change-in-development",
        "--return-code",
        "-cID", "8000038673"});
    }


    @Test
    public void testGetChangeStatusWithBadCredentials() throws Exception {

        thrown.expect(ExitException.class);
        thrown.expect(Matchers.hasRootCause(ODataClientErrorException.class));
        thrown.expect(Matchers.rootCauseMessageContains("401"));

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock(new ODataClientErrorException(StatusLines.UNAUTHORIZED)));

        Commands.main(new String[] {
        "-u", "DOES_NOT_EXIST",
        "-p", "********",
        "-e", SERVICE_ENDPOINT,
        "-t", "SOLMAN",
        "is-change-in-development",
        "-cID", "8000038673"});
    }

    @Test
    public void testGetChangeStatusForNotExistingChange() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("404");
        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock(new ODataClientErrorException(StatusLines.NOT_FOUND)));

        try {
            Commands.main(new String[] {
            "-u", SERVICE_USER,
            "-p", SERVICE_PASSWORD,
            "-e", SERVICE_ENDPOINT,
            "-t", "SOLMAN",
            "is-change-in-development",
            "-cID", "DOES_NOT_EXIST"});
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

        Commands.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "-t", "SOLMAN",
        "is-change-in-development"});
    }

    @Test
    public void testGetChangeStatusPasswordViaStdin() throws Exception {

        InputStream oldIn = System.in;
        System.setIn(new ByteArrayInputStream(SERVICE_PASSWORD.getBytes()));

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        try {
          Commands.main(new String[] {
          "-u", SERVICE_USER,
          "-p", "-",
          "-e", SERVICE_ENDPOINT,
          "-t", "SOLMAN",
          "is-change-in-development",
          "-cID", "8000038673"});
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
          Commands.main(new String[] {
          "-u", SERVICE_USER,
          "-p", "-",
          "-e", SERVICE_ENDPOINT,
          "-t", "SOLMAN",
          "is-change-in-development", 
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
          Commands.main(new String[] {
          "-u", SERVICE_USER,
          "-p", "-",
          "-e", SERVICE_ENDPOINT,
          "-t", "SOLMAN",
          "is-change-in-development",
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

        Commands.main(new String[] {
        "-u", SERVICE_USER,
        "-e", SERVICE_ENDPOINT,
        "-t", "SOLMAN",
        "is-change-in-development",
        "8000038673"});
    }
}
