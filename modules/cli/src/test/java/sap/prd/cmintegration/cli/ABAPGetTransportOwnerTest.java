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

public class ABAPGetTransportOwnerTest extends ABAPBackendTest {

    private AbapClientFactory setupGetTransportMock(Transport t) throws Exception {

        AbapClientFactory factoryMock = createMock(AbapClientFactory.class);
        CMODataAbapClient clientMock = createMock(CMODataAbapClient.class);

        expect(clientMock.getTransport(anyString())).andReturn(t);
        expect(factoryMock.newClient(capture(host), capture(user), capture(password))).andReturn(clientMock);

        replay(factoryMock, clientMock);

        return factoryMock;
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
}

