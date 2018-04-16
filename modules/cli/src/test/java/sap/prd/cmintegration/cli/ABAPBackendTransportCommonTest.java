package sap.prd.cmintegration.cli;

import static com.google.common.collect.Sets.newHashSet;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Maps;
import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.http.CMODataAbapClient;

@RunWith(Parameterized.class)
public class ABAPBackendTransportCommonTest extends ABAPBackendTest {

    private final String command;

    public ABAPBackendTransportCommonTest(String command) {
        this.command = command;
    }
    protected AbapClientFactory setupGetTransportMock(Transport t) throws Exception {

        AbapClientFactory factoryMock = createMock(AbapClientFactory.class);
        CMODataAbapClient clientMock = createMock(CMODataAbapClient.class);

        expect(clientMock.getTransport(anyString())).andReturn(t);
        expect(factoryMock.newClient(capture(host), capture(user), capture(password))).andReturn(clientMock);

        replay(factoryMock, clientMock);

        return factoryMock;
    }

    @Parameters
    public static Set<String> getSubcommands() {
        return newHashSet(
                "get-transport-owner",
                "get-transport-description",
                "get-transport-status",
                "is-transport-modifiable",
                "get-transport-target-system",
                "get-transport-type"
                );
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
                        "-t", "CTS",
                        command,
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
                        "-t", "CTS",
                        command,
                        "-tID", "999"});
    }
}

