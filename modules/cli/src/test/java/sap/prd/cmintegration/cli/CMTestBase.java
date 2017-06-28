package sap.prd.cmintegration.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class CMTestBase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected PrintStream oldOut;
    protected ByteArrayOutputStream result;

    Capture<String> host = Capture.newInstance(),
            user = Capture.newInstance(),
            password = Capture.newInstance(),
            changeId = Capture.newInstance();

    @Before
    public void setup() throws Exception {
        prepareOutputStream();
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(oldOut);
        setMock(null);
    }

    protected void prepareOutputStream(){
        result = new ByteArrayOutputStream();
        oldOut = System.out;
        System.setOut(new PrintStream(result));
    }

    protected static void setMock(ClientFactory mock) throws Exception {
        Field field = ClientFactory.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, mock);
        field.setAccessible(false);
    }
}
