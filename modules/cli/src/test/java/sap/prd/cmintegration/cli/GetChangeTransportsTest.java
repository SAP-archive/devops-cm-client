package sap.prd.cmintegration.cli;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Test;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class GetChangeTransportsTest extends CMTestBase {

    @Test
    public void testStraightForward() throws Exception{

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        GetChangeTransports.main(new String[] {
        "-u", "john.doe",
        "-p", "openSesame",
        "-h", "https://example.org/endpoint/",
        "8000038673"});

        Collection<String> transportIds = asList(IOUtils.toString(result.toByteArray(), "UTF-8").split("\\r?\\n"));
        assertThat(transportIds, contains(
                "L21K900026",
                "L21K900028",
                "L21K900029",
                "L21K90002A",
                "L21K90002B",
                "L21K90002C",
                "L21K90002D",
                "L21K90002E"));
        assertThat(transportIds.size(), is(equalTo(8)));

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }

    private ClientFactory setupMock() throws Exception {

        ArrayList<CMODataTransport> transports = new ArrayList<>();
        transports.add(new CMODataTransport("L21K900026", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K900028", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K900029", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002A", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002B", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002C", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002D", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002E", false, "Description", "Owner"));

        CMODataClient clientMock = EasyMock.createMock(CMODataClient.class);
        expect(clientMock.getChangeTransports(capture(changeId))).andReturn(transports);

        ClientFactory factoryMock = EasyMock.createMock(ClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);

        EasyMock.replay(clientMock, factoryMock);

        return factoryMock;
    }


}
