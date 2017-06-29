package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.junit.Test;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class GetChangeTransportModifiableTest extends CMTestBase {

    @Test
    public void getChangeTransportModifiableStraighForwardForNotModifiableTransport() throws Exception {

        setMock(setupMock(false));
        GetTransportModifiable.main(new String[] {
                "-u", "john.doe",
                "-p", "openSesame",
                "-h", "https://example.org/endpoint/",
                "8000038673", "L21K900026"});

        assertThat(Boolean.valueOf(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8"))),
                is(equalTo(false)));

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }

    @Test
    public void getChangeTransportModifiableStraighForwardForModifiableTransport() throws Exception {

        setMock(setupMock(true));
        GetTransportModifiable.main(new String[] {
                "-u", "john.doe",
                "-p", "openSesame",
                "-h", "https://example.org/endpoint/",
                "8000038673", "L21K900026"});

        assertThat(Boolean.valueOf(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8"))),
                is(equalTo(true)));

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }

    @Test
    public void getChangeTransportModifiableForNotExistingTransport() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Transport 'DOES_NOT_EXIST' not found for change '8000038673'.");

        setMock(setupMock(false));
        GetTransportModifiable.main(new String[] {
                "-u", "john.doe",
                "-p", "openSesame",
                "-h", "https://example.org/endpoint/",
                "8000038673", "DOES_NOT_EXIST"});

        assertThat(Boolean.valueOf(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8"))),
                is(equalTo(false)));

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }

    @Test
    public void getChangeTransportModifiableForNotExistingChange() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("400");

        setMock(setupMock(false, new ODataClientErrorException(
                new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 400, "Bad Request"))));

        try {
            GetTransportModifiable.main(new String[] {
                "-u", "john.doe",
                "-p", "openSesame",
                "-h", "https://example.org/endpoint/",
                "DOES_NOT_EXIST", "NOT_NEEDED"});
        } catch(ODataClientErrorException ex) {
            assertThat(changeId.getValue(), is(equalTo("DOES_NOT_EXIST")));
            throw ex;
        }
    }

    @Test
    public void getChangeTransportModifiableWithoutProvidingTransportId() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("No transportId specified.");

        setMock(setupMock(false, new ODataClientErrorException(
                new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 400, "Bad Request"))));

        GetTransportModifiable.main(new String[] {
                "-u", "john.doe",
                "-p", "openSesame",
                "-h", "https://example.org/endpoint/",
                "8000038673"});
        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }

    private ClientFactory setupMock(boolean isModifiable) throws Exception {
        return setupMock(isModifiable, null);
    }

    private ClientFactory setupMock(boolean isModifiable, Exception ex) throws Exception {
        CMODataClient clientMock = createMock(CMODataClient.class);
        if(ex == null) {
            ArrayList<CMODataTransport> transports = new ArrayList<>();
            transports.add(new CMODataTransport("L21K900026", isModifiable));
            expect(clientMock.getChangeTransports(capture(changeId))).andReturn(transports);
        } else {
            expect(clientMock.getChangeTransports(capture(changeId))).andThrow(ex);
        }
        ClientFactory factoryMock = createMock(ClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);

        replay(clientMock, factoryMock);
        return factoryMock;
    }

    private static String removeCRLF(String str) {
        return str.replaceAll("\\r?\\n$", "");
    }
}
