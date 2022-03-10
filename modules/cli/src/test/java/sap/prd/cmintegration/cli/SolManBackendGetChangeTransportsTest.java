package sap.prd.cmintegration.cli;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import com.sap.cmclient.Transport;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class SolManBackendGetChangeTransportsTest extends SolManBackendCMTransportTestBase {

    @Test
    public void testStraightForward() throws Exception{

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(setupMock());

        Commands.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "get-transports",
        "-cID", "8000038673"});

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

        Commands.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "get-transports", "-m",
        "-cID", "8000038673"});

        Collection<String> transportIds = asList(IOUtils.toString(result.toByteArray(), "UTF-8").split("\\r?\\n"));
        assertThat(transportIds, contains("L21K90002E"));
        assertThat(transportIds.size(), is(equalTo(1)));

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }

    @Test
    public void testHelp() throws Exception {
        Commands.main(new String[] {
                "get-transports",
                "--help"});
        String helpText = IOUtils.toString(result.toByteArray(), "UTF-8");

        assertThat(helpText, new BaseMatcher<String>() {

            String expected = ".*-m,--modifiable-only[\\s]*Returns modifiable transports only.*";
            String actual;

            @Override
            public boolean matches(Object item) {
                if(! (item instanceof String)) {
                    return false;
                }

                actual = (String) item;
                return Pattern.compile(expected, Pattern.MULTILINE).matcher(actual).find();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("Expected regex '%s' not found in '%s'.", expected, actual));
            }
        });
    }

    private SolmanClientFactory setupMock() throws Exception {

        ArrayList<Transport> transports = new ArrayList<>();
        transports.add(new CMODataTransport("L21K900026", "J01~JAVA", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K900028", "J01~JAVA", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K900029", "J01~JAVA", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002A", "J01~JAVA", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002B", "J01~JAVA", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002C", "J01~JAVA", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002D", "J01~JAVA", false, "Description", "Owner"));
        transports.add(new CMODataTransport("L21K90002E", "J01~JAVA", true, "Description", "Owner"));

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
