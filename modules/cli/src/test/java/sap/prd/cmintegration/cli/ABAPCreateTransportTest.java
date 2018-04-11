package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.http.CMODataAbapClient;

public class ABAPCreateTransportTest extends CMABAPTestBase {

    private Capture<Map<String, Object>> transportMap = Capture.newInstance();

    private AbapClientFactory setupCreateTransportMock(Transport result) throws Exception {

        AbapClientFactory factoryMock = createMock(AbapClientFactory.class);
        CMODataAbapClient clientMock = createMock(CMODataAbapClient.class);

        expect(clientMock.createTransport(capture(transportMap))).andReturn(result);
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
    public void testCreateTransport() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        setMock(setupCreateTransportMock(new Transport(m)));

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "create-transport",
                        "-tt", "K",
                        "-d", "desc",
                        "-ts", "A5X"});

        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), is(equalTo("999")));

        Map<String, Object> tMap = transportMap.getValue();

        //
        // We test if the map used for creating the transport on the backend
        // is fine. Testing if the transport instance finally returned matches
        // is tested inside the test for testing the Transport DTO.
        //

        // Id must not be provided in the map. Id is provided from the
        // backend.
        assertThat(tMap.get("Id"), is(equalTo("")));
        assertThat(tMap.get("Description"), is(equalTo("desc")));
        assertThat(tMap.get("TarSystem"), is(equalTo("A5X")));
        assertThat(tMap.get("Type"), is(equalTo("K")));
    }
}

