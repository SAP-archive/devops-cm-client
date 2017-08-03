package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Properties;

import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.apache.olingo.client.api.ODataClient;
import org.easymock.Capture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CMODataClientBaseTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected static class StatusLines {
        private final static ProtocolVersion HTTP_1_1 = new ProtocolVersion("HTTP", 1, 1);
        protected final static BasicStatusLine BAD_REQUEST = new BasicStatusLine(HTTP_1_1, 400, "Bad Request");
        protected final static BasicStatusLine UNAUTHORIZED = new BasicStatusLine(HTTP_1_1, 401, "Unauthorized");
        protected final static BasicStatusLine NOT_FOUND = new BasicStatusLine(HTTP_1_1, 404, "Not Found.");
    }
    protected Capture<URI> address;

    protected CMODataClient examinee;

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

    protected static void setMock(CMODataClient examinee, ODataClient mock) throws Exception {
        Field client = CMODataClient.class.getDeclaredField("client");
        client.setAccessible(true);
        client.set(examinee, mock);
        client.setAccessible(false);
    }


    @Test
    public void testGetShortVersion() throws Exception {

        // we depend on the mvn build. mvn package or similar needs to be executed first.
        File versionFile = new File("target/classes/VERSION");
        assumeTrue(versionFile.exists());

        String actualShortVersion = CMODataClient.getShortVersion(),
               expectedShortVersion = getVersionProperties(versionFile).getProperty("mvnProjectVersion");

        assertThat(expectedShortVersion, is(not(nullValue())));
        assertThat(actualShortVersion, is(equalTo(expectedShortVersion)));
    }

    @Test
    public void testLongShortVersion() throws Exception {

        // we depend on the mvn build. mvn package or similar needs to be executed first.
        File versionFile = new File("target/classes/VERSION");
        assumeTrue(versionFile.exists());

        Properties vProps = getVersionProperties(versionFile);
        String actualLongVersion = CMODataClient.getLongVersion(),
               expectedLongVersion = format("%s : %s",
                vProps.getProperty("mvnProjectVersion"),
                vProps.getProperty("gitCommitId"));

        assertThat(expectedLongVersion, is(not(nullValue())));
        assertThat(actualLongVersion, is(equalTo(expectedLongVersion)));
    }

    private static Properties getVersionProperties(File versionFile) throws IOException {
        try(InputStream is = new FileInputStream(versionFile)) {
            Properties vProps = new Properties();
            vProps.load(is);
            return vProps;
        }
    }
}
