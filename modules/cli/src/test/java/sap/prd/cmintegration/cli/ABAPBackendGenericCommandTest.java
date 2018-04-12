package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.apache.commons.cli.MissingOptionException;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.http.CMODataAbapClient;

public class ABAPBackendGenericCommandTest extends ABAPBackendTest {

    private AbapClientFactory setupGetTransportMock(Transport t) throws Exception {

        AbapClientFactory factoryMock = createMock(AbapClientFactory.class);
        CMODataAbapClient clientMock = createMock(CMODataAbapClient.class);

        expect(clientMock.getTransport(anyString())).andReturn(t);
        expect(factoryMock.newClient(capture(host), capture(user), capture(password))).andReturn(clientMock);

        replay(factoryMock, clientMock);

        return factoryMock;
    }

    @Test
    public void testServerCoordinatesStraightForward() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        setMock(setupGetTransportMock(new Transport(m)));

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "get-transport-owner",
                        "-tID", "999"});

        assertThat(host.getValue(), is(equalTo("http://example.org:8000/endpoint")));
        assertThat(user.getValue(), is(equalTo("me")));
        assertThat(password.getValue(), is(equalTo("openSesame")));
    }

    @Test
    public void testUserNotProvided() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: u");

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "get-transport-owner",
                        "-tID", "999"});
    }

    @Test
    public void testEndpointNotProvided() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: e");

        Commands.main(new String[]
                {       "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "get-transport-owner",
                        "-tID", "999"});
    }

    @Test
    public void testPasswordNotProvided() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: p");

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-t", "ABAP",
                        "get-transport-owner",
                        "-tID", "999"});
    }

    @Test
    public void testBackendTypeNotProvided() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Cannot retrieve backend type.");

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "get-transport-owner",
                        "-tID", "999"});
    }

    @Test
    public void testInvalidBackendTypeProvided() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No enum constant");
        thrown.expectMessage("XXX");

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "XXX",
                        "-tID", "999"});
    }

    @Test
    public void test_InterstingFinding_TransportIDBeforeBackendTypeDoesNotWork() throws Exception {

        //
        // In this case "-tID" is understaod like "-t ID".
        //
        // Since we document that the generic options needs to be provided first - and with that
        // before the subcommand and the subcommand options this does not hurt.
        //

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No enum constant");
        thrown.expectMessage("ID");

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-tID", "999",
                        "-t", "XXX"});
    }
}

