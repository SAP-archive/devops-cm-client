package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Maps;
import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.http.CMODataAbapClient;

public class ABAPBackendUploadFileTest extends CMABAPBackendTestBase {

    private Capture<String> transportIdGetTransport = Capture.newInstance();
    private Capture<String> transportIdUploadFile = Capture.newInstance();
    private Capture<File> uploaded = Capture.newInstance();

    private File upload;

    @Rule
    public TemporaryFolder f = new TemporaryFolder();

    @Before
    public void prepareUploadFile() throws IOException {
        this.upload = f.newFile();
        FileUtils.write(upload, "Hello SAP", Charset.forName("UTF-8"));
    }

    @Test
    public void uploadFileTestStraightForwards() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");

        setMock(setupUploadFileMock(upload, new Transport(m)));

        Commands.main(new String[] {
                        "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "CTS",
                        "upload-file-to-transport",
                        "-tID", "999",
                        upload.getAbsolutePath()
                        });

        assertThat(transportIdGetTransport.getValue(), is(equalTo("999")));
        assertThat(transportIdUploadFile.getValue(), is(equalTo("999")));
        assertThat(uploaded.getValue().getAbsolutePath(), is(equalTo(upload.getAbsolutePath())));
        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), endsWith("/$value"));
    }

    @Test
    public void uploadFileWhichDoesNotExistRaisesException() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot read upload file");

        Commands.main(new String[] {
                        "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "CTS",
                        "upload-file-to-transport",
                        "-tID", "999",
                        "does/not/exist.txt"
                        });
    }

    @Test
    public void uploadFileToNotExistingTransportRaisesException() throws Exception {

        thrown.expect(TransportNotFoundException.class);
        thrown.expectMessage("Transport '999' not found");

        setMock(setupUploadFileMock(upload, null));

        Commands.main(new String[] {
                        "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "CTS",
                        "upload-file-to-transport",
                        "-tID", "999",
                        upload.getAbsolutePath()
                        });
    }

    private AbapClientFactory setupUploadFileMock(File f, Transport t) throws Exception {

        AbapClientFactory factoryMock = createMock(AbapClientFactory.class);
        CMODataAbapClient clientMock = createMock(CMODataAbapClient.class);

        expect(factoryMock.newClient(capture(host), 
                                     capture(user), 
                                     capture(password))).andReturn(clientMock).anyTimes();

        expect(clientMock.getTransport(capture(transportIdGetTransport))).andReturn(t);
        expect(clientMock.upload(capture(transportIdUploadFile), capture(uploaded))).andReturn("theLocation");

        replay(factoryMock, clientMock);

        return factoryMock;
    }
}

