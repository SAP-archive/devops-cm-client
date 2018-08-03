package sap.prd.cmintegration.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class CMTestBase {

    protected final static String SERVICE_USER = System.getProperty("CM_SERVICE_USER", "john.doe"),
                                  SERVICE_PASSWORD = System.getProperty("CM_SERVICE_PASSWORD", "openSesame"),
                                  SERVICE_ENDPOINT = System.getProperty("CM_SERVICE_ENDPOINT", "https://example.org/myEndpoint");

    protected static class StatusLines {
        private final static ProtocolVersion HTTP_1_1 = new ProtocolVersion("HTTP", 1, 1);
        protected final static BasicStatusLine BAD_REQUEST = new BasicStatusLine(HTTP_1_1, 400, "Bad Request");
        protected final static BasicStatusLine UNAUTHORIZED = new BasicStatusLine(HTTP_1_1, 401, "Unauthorized");
        protected final static BasicStatusLine NOT_FOUND = new BasicStatusLine(HTTP_1_1, 404, "Not Found.");
    }

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

    protected void prepareOutputStream(){
        result = new ByteArrayOutputStream();
        oldOut = System.out;
        System.setOut(new PrintStream(result));
    }

    /*
     * Intended for being used with a single line string.
     */
    protected static String removeCRLF(String str) {
        return str.replaceAll("\\r?\\n$", "");
    }
}
