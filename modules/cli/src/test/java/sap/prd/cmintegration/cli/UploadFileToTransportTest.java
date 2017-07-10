package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class UploadFileToTransportTest extends CMTestBase {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Capture<String>
            transportId = Capture.newInstance(),
            filePath = Capture.newInstance(),
            applicationId = Capture.newInstance();

    @Test
    public void testUploadFileStraightForward() throws Exception {

        //
        // TODO: This test case has not been verified against the real backend.
        // At the moment when writing this the response was http 400 with message
        // "L21K90002G not found.". Despite this message such an transport request exists.
        // According to the resposibles backend developers this is caused by the fact that
        // this transport request was not created in the context of a change request.
        // Discussion pending.
        //
        setMock(setupMock());

        String fileName = UUID.randomUUID().toString() + ".txt";
        File upload = tmp.newFile(fileName);
        FileUtils.touch(upload);

        UploadFileToTransport.main(new String[] {
        "-u", "john.doe",
        "-p", "openSesame",
        "-h", "https://example.org/endpoint/",
        "L21K90002G", "HCP", upload.getAbsolutePath()
        });

        assertThat(transportId.getValue(), is(equalTo("L21K90002G")));
        assertThat(filePath.getValue(), endsWith(fileName));
        assertThat(applicationId.getValue(), is(equalTo("HCP")));
    }

    @Test
    public void testUploadFileFailedDueToMissingFile() throws Exception {

        final String fileName = UUID.randomUUID().toString() + ".txt";

        File upload = tmp.newFile(fileName);
        if(upload.exists()) {
            if(!upload.delete()) {
                throw new IOException(format("Cannot delete file '%s'.", upload));
            }
        }

        assertThat("Upload file which should not be present according to test intention"
                + "exists already before test.", upload.exists(), is(equalTo(false)));

        /*
         * thrown is set here after assert above. Otherwise a failed assert would end up
         * in an error message about an unexpected exception. This would be hard to understand.
         */
        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage(new BaseMatcher<String>() {

            private String expected = "Cannot read file .*" + fileName + ".*";

            @Override
            public boolean matches(Object item) {
                return ((String)item).matches(expected);
            }
            @Override
            public void describeTo(Description description) {
                description.appendText(format("Expected text not found in exception message: \"%s\".", expected));
            }
        });

        setMock(setupMock());


        UploadFileToTransport.main(new String[] {
        "-u", "john.doe",
        "-p", "openSesame",
        "-h", "https://example.org/endpoint/",
        "L21K90002G", "HCP", upload.getAbsolutePath()
        });

    }

    private ClientFactory setupMock() throws IOException {

        CMODataClient clientMock = EasyMock.createMock(CMODataClient.class);
        clientMock.uploadFileToTransport(capture(transportId),
            capture(filePath), capture(applicationId));
        expectLastCall();

        ClientFactory factoryMock = EasyMock.createMock(ClientFactory.class);
            expect(factoryMock
                   .newClient(capture(host),
                           capture(user),
                           capture(password))).andReturn(clientMock);

        EasyMock.replay(clientMock, factoryMock);

        return factoryMock;
    }
}
