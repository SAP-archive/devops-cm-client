package sap.prd.cmintegration.cli;

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

import org.apache.commons.cli.MissingOptionException;
import org.easymock.EasyMock;
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

    private CMODataClient clientMock;
    private CMODataChange changeMock;

    private PrintStream oldOut;
    private ByteArrayOutputStream result;

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
        expect(clientMock.getChange("8000038673")).andReturn(changeMock);

        EasyMock.replay(changeMock, clientMock);
    }

    private void prepareOutputStream(){
        result = new ByteArrayOutputStream();
        oldOut = System.out;
        System.setOut(new PrintStream(result));
    }

    @Test
    public void testGetChangeStatusStraightForward() throws Exception {

        //
        // Comment line below in order to go against the real back-end as specified via -h
        GetChangeStatus.client = clientMock;

        GetChangeStatus.main(new String[] {
        "-c", "8000038673",
        "-u", "john.doe",
        "-p", "openSesame",
        "-h", "https://example.org/endpoint/"});

        assertThat(GetChangeStatus.getChangeId(), is(equalTo("8000038673")));
        assertThat(GetChangeStatus.getUser(), is(equalTo("john.doe")));
        assertThat(GetChangeStatus.getPassword(), is(equalTo("openSesame")));
        assertThat(GetChangeStatus.getHost(), is(equalTo("https://example.org/endpoint/")));

        assertThat(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.toByteArray()), "UTF-8")).readLine(), equalTo("E0002"));
    }

    @Test
    public void testGetChangeStatusPasswordViaStdin() throws Exception {

        InputStream oldIn = System.in;
        System.setIn(new ByteArrayInputStream("openSesame".getBytes()));

        //
        // Comment line below in order to go against the real back-end as specified via -h
        GetChangeStatus.client = clientMock;

        try {
          GetChangeStatus.main(new String[] {
          "-c", "8000038673",
          "-u", "john.doe",
          "-p", "-",
          "-h", "https://example.org/endpoint/"});
        } finally {
            System.setIn(oldIn);
        }

        assertThat(GetChangeStatus.getPassword(), is(equalTo("openSesame")));
    }

    @Test
    public void testGetChangeStatusNoPassword() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: p");

        //
        // Comment line below in order to go against the real back-end as specified via -h
        GetChangeStatus.client = clientMock;

        GetChangeStatus.main(new String[] {
        "-c", "8000038673",
        "-u", "john.doe",
        "-h", "https://example.org/endpoint/"});
    }

}
