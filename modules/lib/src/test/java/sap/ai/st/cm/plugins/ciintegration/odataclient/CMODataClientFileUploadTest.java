package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static sap.ai.st.cm.plugins.ciintegration.odataclient.Matchers.carriesStatusCode;
import static sap.ai.st.cm.plugins.ciintegration.odataclient.Matchers.hasRootCause;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.ODataPayloadManager;
import org.apache.olingo.client.api.communication.request.cud.CUDRequestFactory;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.request.retrieve.RetrieveRequestFactory;
import org.apache.olingo.client.api.communication.request.streamed.ODataMediaEntityUpdateRequest;
import org.apache.olingo.client.api.communication.response.ODataResponse;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.http.HttpClientException;
import org.apache.olingo.client.core.ODataClientImpl;
import org.apache.olingo.commons.api.format.ContentType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CMODataClientFileUploadTest extends CMODataClientBaseTest {

    private File testFile;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setup() throws Exception {
        super.setup();
        prepareTestFile();
    }

    @After
    public void tearDown() throws Exception {
        testFile = null;
        super.tearDown();
    }

    private void prepareTestFile() throws IOException {
        testFile = tmp.newFile(UUID.randomUUID().toString() + ".txt");
        FileUtils.write(testFile, "{\"description\": \"Created by unit test.\"}");
    }

    @Test
    public void testUploadFileStraightForward() throws Exception {

        // comment line below for testing against real backend.
        // Assert for the captures below needs to be commented also in this case.
        setMock(examinee, setupUploadFileSucceedsMock());

        examinee.uploadFileToTransport("8000042445", "L21K900035", testFile.getAbsolutePath(), "HCP");

        assertThat(address.getValue().toASCIIString(),
                is(equalTo("https://example.org/endpoint/Files(ChangeID='8000042445',TransportID='L21K900035',FileID='" +
                   testFile.getName() + "',ApplicationID='HCP')")));
    }

    @Test
    public void testUploadFileToClosedTransportFails() throws Exception {

        /*
         * Details about the reason are provided by the server, but they gets lost during
         * parsing the odata error response since the xml namespace used by the server does not
         * match the xml namespace expected by the client. For the message element in the response
         * there is no namespace used at all. But there is the namespace
         * "http://docs.oasis-open.org/odata/ns/metadata" expected by the client.
         * See: org.apache.olingo.client.core.serialization.AtomDeserializer.
         *
         * Hence we cannot check for more details.
         */
        thrown.expect(HttpClientException.class);

        // comment line below for testing against real backend.
        setMock(examinee, setupUploadFileFailsMock());

        try {
            //transport 'L21K900026' exists, but is closed.
             examinee.uploadFileToTransport("8000038673", "L21K900026", testFile.getAbsolutePath(), "HCP");
        } catch (HttpClientException e) {
            assertThat(e, hasRootCause(ODataClientErrorException.class));
            assertThat(e, carriesStatusCode(400));
            throw e;
        }
    }

    @Test
    public void testUploadFileToNonExistingTransportFails() throws Exception {

        /*
         * Details about the reason are provided by the server, but they gets lost during
         * parsing the odata error response since the xml namespace used by the server does not
         * match the xml namespace expected by the client. For the message element in the response
         * there is no namespace used at all. But there is the namespace
         * "http://docs.oasis-open.org/odata/ns/metadata" expected by the client.
         * See: org.apache.olingo.client.core.serialization.AtomDeserializer.
         *
         * Hence we cannot check for more details.
         */
        thrown.expect(HttpClientException.class);

        // comment line below for testing against real backend.
        setMock(examinee, setupUploadFileFailsMock());

        try {
            //transport 'L21K900XFG' does not exist
            examinee.uploadFileToTransport("8000042445", "L21K900XFG", testFile.getAbsolutePath(), "HCP"); 
        } catch (HttpClientException e) {
            assertThat(e, hasRootCause(ODataClientErrorException.class));
            assertThat(e, carriesStatusCode(400));
            throw e;
        }
    }

    @Test
    public void testUploadFileCalledOnClosedClient() throws Exception{
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This instance of CMODataClient has been closed");
        examinee.close();
        examinee.uploadFileToTransport("xx", "xx", "xx", "xx");
    }

    private ODataClient setupUploadFileSucceedsMock() {
        return setupMock(null);
    }

    private ODataClient setupUploadFileFailsMock() {
        return setupMock(new HttpClientException(
                    new RuntimeException(new ODataClientErrorException(
                        StatusLines.BAD_REQUEST))));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ODataClient setupMock(Exception e) {

        class MockHelpers {

            ODataResponse setupResponseMock() {
                ODataResponse responseMock = createMock(ODataResponse.class);
                expect(responseMock.getStatusCode()).andReturn(204);
                responseMock.close(); expectLastCall();
                replay(responseMock);
                return responseMock;
            }

            RetrieveRequestFactory setupCSRFResponseMock() {
                ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> responseMock = createMock(ODataRetrieveResponse.class);
                expect(responseMock.getHeader("X-CSRF-Token")).andReturn(Arrays.asList("yyy"));

                ODataEntitySetIteratorRequest<ClientEntitySet,ClientEntity> oDataEntitySetIteratorRequestMock = createMock(ODataEntitySetIteratorRequest.class);
                expect(oDataEntitySetIteratorRequestMock.addCustomHeader("X-CSRF-Token", "Fetch")).andReturn(oDataEntitySetIteratorRequestMock);
                expect(oDataEntitySetIteratorRequestMock.setAccept("application/xml")).andReturn(oDataEntitySetIteratorRequestMock);
                expect(oDataEntitySetIteratorRequestMock.execute()).andReturn(responseMock);

                RetrieveRequestFactory retrieveRequestFactoryMock = createMock(RetrieveRequestFactory.class);
                expect(retrieveRequestFactoryMock.getEntitySetIteratorRequest(EasyMock.anyObject(URI.class))).andReturn(oDataEntitySetIteratorRequestMock);

                replay(responseMock, oDataEntitySetIteratorRequestMock, retrieveRequestFactoryMock);
                return retrieveRequestFactoryMock;
            }

            ODataMediaEntityUpdateRequest setupEntityUpdateRequestMock(ODataPayloadManager payloadManagerMock) {
                ODataMediaEntityUpdateRequest entityUpdateRequestMock = createMock(ODataMediaEntityUpdateRequest.class);
                expect(entityUpdateRequestMock.addCustomHeader("x-csrf-token", "yyy")).andReturn(entityUpdateRequestMock);
                entityUpdateRequestMock.setFormat(ContentType.APPLICATION_ATOM_XML); expectLastCall();
                expect(entityUpdateRequestMock.setContentType("text/plain")).andReturn(entityUpdateRequestMock);
                expect(entityUpdateRequestMock.payloadManager()).andReturn(payloadManagerMock);
                replay(entityUpdateRequestMock);
                return entityUpdateRequestMock;
            }
        }

        MockHelpers helpers = new MockHelpers();

        ODataPayloadManager payloadManagerMock = createMock(ODataPayloadManager.class);
        if(e != null) {
            expect(payloadManagerMock.getResponse()).andThrow(e);
        } else {
            expect(payloadManagerMock.getResponse()).andReturn(helpers.setupResponseMock());
        }

        CUDRequestFactory cudRequestFactoryMock = createMock(CUDRequestFactory.class);
        expect(cudRequestFactoryMock.getMediaEntityUpdateRequest(capture(address), anyObject(InputStream.class)))
            .andReturn(helpers.setupEntityUpdateRequestMock(payloadManagerMock));

        ODataClient clientMock = createMockBuilder(ODataClientImpl.class)
                .addMockedMethod("getConfiguration")
                .addMockedMethod("getRetrieveRequestFactory")
                .addMockedMethod("getCUDRequestFactory").createMock();

        expect(clientMock.getCUDRequestFactory()).andReturn(cudRequestFactoryMock);
        expect(clientMock.getConfiguration()).andReturn(MockHelper.getConfiguration()).times(2);
        expect(clientMock.getRetrieveRequestFactory()).andReturn(helpers.setupCSRFResponseMock());

        replay(payloadManagerMock, cudRequestFactoryMock, clientMock);
        return clientMock;
    }
}
