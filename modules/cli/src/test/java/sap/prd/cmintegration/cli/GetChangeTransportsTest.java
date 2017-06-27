package sap.prd.cmintegration.cli;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class GetChangeTransportsTest {

    private ClientFactory factoryMock;

    Capture<String> host = Capture.newInstance(),
            user = Capture.newInstance(),
            password = Capture.newInstance(),
            changeId = Capture.newInstance();

    private PrintStream oldOut;
    private ByteArrayOutputStream result;

    @Before
    public void setup() throws Exception {
        setupMock();
        prepareOutputStream();
    }

    @After
    public void tearDown() {
        System.setOut(oldOut);
    }

    @Test
    public void testStraightForward() throws Exception{

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(factoryMock);

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

    private void prepareOutputStream(){
        result = new ByteArrayOutputStream();
        oldOut = System.out;
        System.setOut(new PrintStream(result));
    }

    private static void setMock(ClientFactory mock) throws Exception {
        Field field = ClientFactory.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, mock);
        field.setAccessible(false);
    }

    private void setupMock() throws Exception {

        ArrayList<CMODataTransport> transports = new ArrayList<>();
        transports.add(new CMODataTransport("L21K900026", false));
        transports.add(new CMODataTransport("L21K900028", false));
        transports.add(new CMODataTransport("L21K900029", false));
        transports.add(new CMODataTransport("L21K90002A", false));
        transports.add(new CMODataTransport("L21K90002B", false));
        transports.add(new CMODataTransport("L21K90002C", false));
        transports.add(new CMODataTransport("L21K90002D", false));
        transports.add(new CMODataTransport("L21K90002E", false));

        CMODataClient clientMock = EasyMock.createMock(CMODataClient.class);
        expect(clientMock.getChangeTransports(capture(changeId))).andReturn(transports);

        factoryMock = EasyMock.createMock(ClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);

        EasyMock.replay(clientMock, factoryMock);
    }


}
