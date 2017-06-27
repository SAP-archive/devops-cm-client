package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.io.IOUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class GetChangeStatusTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ClientFactory factoryMock;
    private CMODataClient clientMock;
    private CMODataChange changeMock;

    private PrintStream oldOut;
    private ByteArrayOutputStream result;

    Capture<String> host = Capture.newInstance(),
            user = Capture.newInstance(),
            password = Capture.newInstance(),
            changeId = Capture.newInstance();

    @Before
    public void setup() throws Exception{
        setupMock();
        prepareOutputStream();
    }

    @After
    public void tearDown() {
        System.setOut(oldOut);
    }

    private void setupMock() throws Exception {
        changeMock = EasyMock.createMock(CMODataChange.class);
        expect(changeMock.getStatus()).andReturn("E0002");

        clientMock = EasyMock.createMock(CMODataClient.class);
        expect(clientMock.getChange(capture(changeId))).andReturn(changeMock);

        factoryMock = EasyMock.createMock(ClientFactory.class);
        expect(factoryMock
                .newClient(capture(host),
                        capture(user),
                        capture(password))).andReturn(clientMock);

        EasyMock.replay(changeMock, clientMock, factoryMock);
    }

    private void prepareOutputStream(){
        result = new ByteArrayOutputStream();
        oldOut = System.out;
        System.setOut(new PrintStream(result));
    }

    @Test
    public void testPrintHelp() throws Exception {
        GetChangeStatus.main(new String[] {"--help"});
        String help = IOUtils.toString(result.toByteArray(), "UTF-8");
        assertThat(help, Matchers.containsString("Prints this help."));
    }

    @Test
    public void testGetChangeStatusStraightForward() throws Exception {

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(factoryMock);

        GetChangeStatus.main(new String[] {
        "-u", "john.doe",
        "-p", "openSesame",
        "-h", "https://example.org/endpoint/",
        "8000038673"});

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
        assertThat(user.getValue(), is(equalTo("john.doe")));
        assertThat(password.getValue(), is(equalTo("openSesame")));
        assertThat(host.getValue(), is(equalTo("https://example.org/endpoint/")));

        assertThat(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.toByteArray()), "UTF-8")).readLine(), equalTo("E0002"));
    }

    @Test
    public void testGetChangeStatusWithoutChangeId() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("No changeId specified.");
        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(factoryMock);

        GetChangeStatus.main(new String[] {
        "-u", "john.doe",
        "-p", "openSesame",
        "-h", "https://example.org/endpoint/"});
    }

    @Test
    public void testGetChangeStatusPasswordViaStdin() throws Exception {

        InputStream oldIn = System.in;
        System.setIn(new ByteArrayInputStream("openSesame".getBytes()));

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(factoryMock);

        try {
          GetChangeStatus.main(new String[] {
          "-u", "john.doe",
          "-p", "-",
          "-h", "https://example.org/endpoint/",
          "8000038673"});
        } finally {
            System.setIn(oldIn);
        }

        assertThat(password.getValue(), is(equalTo("openSesame")));
    }

    @Test
    public void testGetChangeStatusNoPassword() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: p");

        //
        // Comment line below in order to go against the real back-end as specified via -h
        setMock(factoryMock);

        GetChangeStatus.main(new String[] {
        "-u", "john.doe",
        "-h", "https://example.org/endpoint/",
        "8000038673"});
    }

    private static void setMock(ClientFactory mock) throws Exception {
        Field field = ClientFactory.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, mock);
        field.setAccessible(false);
    }
}
