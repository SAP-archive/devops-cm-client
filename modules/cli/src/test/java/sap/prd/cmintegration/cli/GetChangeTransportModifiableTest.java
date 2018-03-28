package sap.prd.cmintegration.cli;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.junit.Test;

public class GetChangeTransportModifiableTest extends CMTransportTestBase {

    @Test
    public void getChangeTransportModifiableStraighForwardForNotModifiableTransport() throws Exception {

        setMock(setupMock("L21K900026", "xOwner", "xDescription", false));
        GetTransportModifiableSOLMAN.main(new String[] {
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "-t", "SOLMAN",
                "dummy-cmd",
                "-cID", "8000038673", "-tID", "L21K900026"});

        assertThat(Boolean.valueOf(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8"))),
                is(equalTo(false)));

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }

    @Test
    public void getChangeTransportModifiableStraighForwardForModifiableTransport() throws Exception {

        setMock(setupMock("L21K900026", "xOwner", "xDescription", true));
        GetTransportModifiableSOLMAN.main(new String[] {
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "-t", "SOLMAN",
                "dummy-cmd",
                "-cID", "8000038673", "-tID", "L21K900026"});

        assertThat(Boolean.valueOf(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8"))),
                is(equalTo(true)));

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }

    @Test
    public void getChangeTransportModifiableForNotExistingTransport() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Transport 'DOES_NOT_EXIST' not found for change '8000038673'.");

        setMock(setupMock("L21K900026", "xOwner", "xDescription", false));
        GetTransportModifiableSOLMAN.main(new String[] {
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "-t", "SOLMAN",
                "dummy-cmd",
                "-cID", "8000038673", "-tID", "DOES_NOT_EXIST"});
    }

    @Test
    public void getChangeTransportModifiableForNotExistingChange() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("400");

        //Comment line and asserts for the captures below in order to run against real back-end.
        setMock(setupMock(new ODataClientErrorException(StatusLines.BAD_REQUEST)));

        try {
            GetTransportModifiableSOLMAN.main(new String[] {
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "-t", "SOLMAN",
                "dummy-cmd",
                "-cID", "DOES_NOT_EXIST", "-tID", "NOT_NEEDED"});
        } catch(ODataClientErrorException ex) {
            assertThat(changeId.getValue(), is(equalTo("DOES_NOT_EXIST")));
            throw ex;
        }
    }

    @Test
    public void getChangeTransportModifiableWithoutProvidingTransportId() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("No transportId specified.");

        GetTransportModifiableSOLMAN.main(new String[] {
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "-t", "SOLMAN",
                "dummy-cmd",
                "-cID", "8000038673"});
    }
}
