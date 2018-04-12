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
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.http.CMODataAbapClient;

public class ABAPTransportTest extends ABAPBackendTest {

    private AbapClientFactory setupGetTransportMock(Transport t) throws Exception {

        AbapClientFactory factoryMock = createMock(AbapClientFactory.class);
        CMODataAbapClient clientMock = createMock(CMODataAbapClient.class);

        expect(clientMock.getTransport(anyString())).andReturn(t);
        expect(factoryMock.newClient(capture(host), capture(user), capture(password))).andReturn(clientMock);

        replay(factoryMock, clientMock);

        return factoryMock;
    }


    @Test
    public void testTransportIdNotProvided() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: tID");

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "get-transport-owner"});
    }

    @Test
    public void testGetTransportOwnerStraightForward() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        m.put("Owner", "Admin");
        setMock(setupGetTransportMock(new Transport(m)));

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "get-transport-owner",
                        "-tID", "999"});

        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), is(equalTo("Admin")));
    }

    @Test
    public void testGetTransportDescriptionStraightForward() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        m.put("Description", "desc");
        setMock(setupGetTransportMock(new Transport(m)));

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "get-transport-description",
                        "-tID", "999"});

        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), is(equalTo("desc")));
    }

    @Test
    public void testLookupNotExistingTransportRaisesTransportNotFoundException() throws Exception {

        thrown.expect(TransportNotFoundException.class);
        thrown.expectMessage("Transport '999' not found.");

        setMock(setupGetTransportMock(null));

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "get-transport-owner",
                        "-tID", "999"});
    }

    @Test
    public void testUnmatchedTransportRaisesCMCommandLineException() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("TransportId of resolved transport ('998') does not match requested transport id ('999').");

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "998");

        setMock(setupGetTransportMock(new Transport(m)));

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "get-transport-owner",
                        "-tID", "999"});
    }

    @Test
    public void testCreateTransportFromMap() {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        m.put("Description", "desc");
        m.put("TarSystem", "A5X");
        m.put("Type", "K");

        Transport t = new Transport(m);

        assertThat(t.getTransportID(), is(equalTo("999")));
        assertThat(t.getDescription(), is(equalTo("desc")));
        assertThat(t.getTargetSystem(), is(equalTo("A5X")));
        assertThat(t.getType(), is(equalTo("K")));
    }

    @Test
    public void testTransportWithoutIdRaisesIllegalArgumentException() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Key 'Id' must not be null");

        // The map created below does not have an id member
        new Transport(Maps.newHashMap());
    }
}

