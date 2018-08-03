package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.apache.commons.cli.MissingOptionException;
import org.easymock.Capture;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.http.CMODataAbapClient;

public class ABAPBackendImportTransportTest extends ABAPBackendTest {

    private Capture<String> transportId = Capture.newInstance(),
                            systemId = Capture.newInstance();

    private AbapClientFactory setupImportTransportMock(Transport t) throws Exception {

        AbapClientFactory factoryMock = createMock(AbapClientFactory.class);
        CMODataAbapClient clientMock = createMock(CMODataAbapClient.class);

        expect(clientMock.importTransport(capture(systemId), capture(transportId))).andReturn(t);
        expect(factoryMock.newClient(capture(host), capture(user), capture(password))).andReturn(clientMock);

        replay(factoryMock, clientMock);

        return factoryMock;
    }

    @Test
    public void testImportTransportStraightForward() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");

        Transport t = new Transport(m);

        setMock(setupImportTransportMock(t));

        Commands.main(new String[] {
                "-e", "http://example.org:8000/endpoint",
                "-u", "me",
                "-p", "openSesame",
                "-t", "CTS",
                "import-transport",
                "-tID", "999",
                "-ts", "A5X"
                });

        assertThat(transportId.getValue(), is(equalTo("999")));
        assertThat(systemId.getValue(), is(equalTo("A5X")));
    }

    @Test
    public void testImportTransportWithoutTargetSystemRaisesException() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: ts");

        Commands.main(new String[] {
                "-e", "http://example.org:8000/endpoint",
                "-u", "me",
                "-p", "openSesame",
                "-t", "CTS",
                "import-transport",
                "-tID", "999"
                });
    }

    @Test
    public void testImportTransportWithoutTransportIdRaisesException() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: tID");

        Commands.main(new String[] {
                "-e", "http://example.org:8000/endpoint",
                "-u", "me",
                "-p", "openSesame",
                "-t", "CTS",
                "import-transport",
                "-ts", "A5X"
                });
    }
}

