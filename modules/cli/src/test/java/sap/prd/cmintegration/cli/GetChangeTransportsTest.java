package sap.prd.cmintegration.cli;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Test;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class GetChangeTransportsTest extends CMTestBase {

    @Test
    public void testStraightForward() throws Exception{

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        GetChangeTransports.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "dummy-cmd",
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

    @Test
    public void testModifiablesOnly() throws Exception{

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        GetChangeTransports.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "dummy-cmd", "-m",
        "8000038673"});

        Collection<String> transportIds = asList(IOUtils.toString(result.toByteArray(), "UTF-8").split("\\r?\\n"));
        assertThat(transportIds, contains("L21K90002E"));
        assertThat(transportIds.size(), is(equalTo(1)));

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }

    @Test
    public void testHelp() throws Exception {
        GetChangeTransports.main(new String[] {"--help"});
        String helpText = IOUtils.toString(result.toByteArray(), "UTF-8");
        assertThat(helpText, containsString("-m,--modifiable-only   Returns modifiable transports only."));
    }

    private SolmanClientFactory setupMock() throws Exception {

        ArrayList<CMODataTransport> transports = new ArrayList<>();
        transports.add(new CMODataTransport("L21K900026", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K900028", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K900029", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002A", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002B", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002C", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002D", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002E", true, "Description", "Owner"));

        CMODataSolmanClient clientMock = createMock(CMODataSolmanClient.class);
        expect(clientMock.getChangeTransports(capture(changeId))).andReturn(transports);

        SolmanClientFactory factoryMock = EasyMock.createMock(SolmanClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);
        clientMock.close(); expectLastCall();

        replay(clientMock, factoryMock);

        return factoryMock;
    }


}
