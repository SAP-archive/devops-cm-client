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
import org.apache.commons.io.IOUtils;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.http.CMODataAbapClient;

public class ABAPExportTransportTest extends CMABAPTestBase {

    private Capture<String> transportId = Capture.newInstance();

    private AbapClientFactory setupExportTransportMock(Transport t) throws Exception {

        AbapClientFactory factoryMock = createMock(AbapClientFactory.class);
        CMODataAbapClient clientMock = createMock(CMODataAbapClient.class);

        expect(clientMock.releaseTransport(capture(transportId))).andReturn(t);
        expect(factoryMock.newClient(capture(host), capture(user), capture(password))).andReturn(clientMock);

        replay(factoryMock, clientMock);

        return factoryMock;
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(oldOut);
        setMock(null);
    }

    @Test
    public void testExportTransportStraightForward() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");

        Transport t = new Transport(m);

        setMock(setupExportTransportMock(t));

        Commands.main(new String[] {
                "-e", "http://example.org:8000/endpoint",
                "-u", "me",
                "-p", "openSesame",
                "-t", "ABAP",
                "export-transport",
                "-tID", "999"});

        assertThat(transportId.getValue(), is(equalTo("999")));
        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), is(equalTo("999")));
    }

    @Test
    public void testExportWithoutTransportIdRaisesException() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: tID");

        Commands.main(new String[] {
                "-e", "http://example.org:8000/endpoint",
                "-u", "me",
                "-p", "openSesame",
                "-t", "ABAP",
                "export-transport"
                });
    }
}

