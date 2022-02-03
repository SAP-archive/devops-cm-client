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
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class SolManBackendUploadFileToTransportTest extends CMSolmanTestBase {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Capture<String>
            transportId = Capture.newInstance(),
            filePath = Capture.newInstance(),
            applicationId = Capture.newInstance();

    @Test
    public void testUploadFileStraightForward() throws Exception {

        setMock(setupMock());

        String fileName = UUID.randomUUID().toString() + ".txt";
        File upload = tmp.newFile(fileName);
        FileUtils.touch(upload);

        Commands.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "upload-file-to-transport",
        "-cID", "8000042445", "-tID", "L21K90002J", "HCP", upload.getAbsolutePath()
        });

        assertThat(changeId.getValue(), is(equalTo("8000042445")));
        assertThat(transportId.getValue(), is(equalTo("L21K90002J")));
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
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot read upload file");

        setMock(setupMock());

        Commands.main(new String[] {
        "-u", SERVICE_USER,
        "-p", SERVICE_PASSWORD,
        "-e", SERVICE_ENDPOINT,
        "upload-file-to-transport",
        "-cID", "8000042445", "-tID", "L21K90002J", "HCP", upload.getAbsolutePath()
        });

    }

    private SolmanClientFactory setupMock() throws Exception {

        CMODataSolmanClient clientMock = EasyMock.createMock(CMODataSolmanClient.class);

        expect(clientMock.getChangeTransports(EasyMock.anyString())).andReturn(Arrays.asList(new CMODataTransport("L21K90002J", "J01~JAVA", true, "desc", "me")));
        clientMock.uploadFileToTransport(capture(changeId), capture(transportId),
            capture(filePath), capture(applicationId)); expectLastCall();
        clientMock.close(); expectLastCall().anyTimes();

        SolmanClientFactory factoryMock = EasyMock.createMock(SolmanClientFactory.class);
            expect(factoryMock
                   .newClient(capture(host),
                           capture(user),
                           capture(password))).andReturn(clientMock).anyTimes();

        EasyMock.replay(clientMock, factoryMock);

        return factoryMock;
    }
}
