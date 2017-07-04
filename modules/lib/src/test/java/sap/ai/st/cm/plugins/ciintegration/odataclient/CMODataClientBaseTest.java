package sap.ai.st.cm.plugins.ciintegration.odataclient;

import java.net.URI;

import org.apache.http.ProtocolVersion;
import org.easymock.Capture;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class CMODataClientBaseTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected final static ProtocolVersion HTTP_1_1 = new ProtocolVersion("HTTP", 1, 1);

    protected Capture<URI> address;

    protected CMODataClient examinee;

    protected CMODataClientBaseTest() {
    }

    protected void setup() throws Exception {

        address = Capture.newInstance();

        examinee = new CMODataClient(
                "https://example.org/endpoint",
                "john.doe",
                "openSesame");

    }

    protected void tearDown() throws Exception {
        examinee = null;
        address = null;
    }

}
