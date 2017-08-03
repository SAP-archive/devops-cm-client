package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static sap.ai.st.cm.plugins.ciintegration.odataclient.Matchers.carriesStatusCode;
import static sap.ai.st.cm.plugins.ciintegration.odataclient.MockHelper.getConfiguration;

import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.invoke.InvokeRequestFactory;
import org.apache.olingo.client.api.communication.request.invoke.ODataInvokeRequest;
import org.apache.olingo.client.api.communication.response.ODataInvokeResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.core.ODataClientImpl;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMODataClientReleaseTransportTest extends CMODataClientBaseTest {

    Capture<String> contentType = Capture.newInstance();

    @Before
    public void setup() throws Exception {
        super.setup();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testReleaseTransportStraightForward() throws Exception {

        /*
         * comment line below for testing against real backend.
         *
         * Of course against the real back-end it works only once. A transport containing at least
         * one item in the object list needs to be prepared in advance. Other test might be used
         * for that.
         *
         * Assert for the captures below needs to be commented also in this case.
         */
        setMock(examinee, setupMock());

        examinee.releaseDevelopmentTransport("8000042445", "L21K90002K");

        Assert.assertThat(address.getValue().toASCIIString(),
                is(equalTo("https://example.org/endpoint/releaseTransport?ChangeID='8000042445'&TransportID='L21K90002K'")));
    }

    @Test
    public void testReleaseTransportFailsDueTransportHasAlreadyBeenReleased() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expect(carriesStatusCode(400)); // TODO: 404 would be better

        setMock(examinee, setupMock(new ODataClientErrorException(
                StatusLines.BAD_REQUEST)));
        examinee.releaseDevelopmentTransport("8000038673", "L21K900026");
    }

    @Test
    public void testReleaseTransportFailsDueToNotExistingChange() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expect(carriesStatusCode(400)); // TODO: 404 would be better

        setMock(examinee, setupMock(new ODataClientErrorException(
                StatusLines.BAD_REQUEST)));
        examinee.releaseDevelopmentTransport("CHANGE_ID_DOES_NOT_EXIST", "TRANSPORT_REQUEST_DOES_ALSO_NOT_EXIST");
    }

    @Test
    public void testReleaseTransportFailsDueToNotExistingTransport() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expect(carriesStatusCode(400)); // TODO: 404 would be better

        setMock(examinee, setupMock(new ODataClientErrorException(
                StatusLines.BAD_REQUEST)));

        examinee.releaseDevelopmentTransport("8000038673", "DOES_NOT_EXIST");
    }

    @Test
    public void testReleaseTransportWithoutChangeID() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ChangeID is null or blank:");
        examinee.releaseDevelopmentTransport("", "");
    }

    @Test
    public void testReleaseTransportWithoutTransportID() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("TransportID is null or blank:");
        examinee.releaseDevelopmentTransport("xx", "");
    }

    @Test
    public void testReleaseTransportOnClosedClient() throws Exception{
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This instance of CMODataClient has been closed");
        examinee.close();
        examinee.releaseDevelopmentTransport("xx", "xx");
    }

    private ODataClient setupMock() {
        return setupMock(null);
    }

    @SuppressWarnings("unchecked")
    private ODataClient setupMock(Exception e) {

        ODataInvokeRequest<ClientEntity> functionInvokeRequest = createMock(ODataInvokeRequest.class);
        expect(functionInvokeRequest.setAccept(capture(contentType))).andReturn(functionInvokeRequest);

        if(e != null) {
            expect(functionInvokeRequest.execute())
                .andThrow(e);
        } else {
            ODataInvokeResponse<ClientEntity> responseMock = createMock(ODataInvokeResponse.class);
            expect(responseMock.getStatusCode()).andReturn(200);
            responseMock.close(); expectLastCall();
            replay(responseMock);
            expect(functionInvokeRequest.execute()).andReturn(responseMock);
        }

        InvokeRequestFactory invokeRequestFactoryMock = createMock(InvokeRequestFactory.class);
        expect(invokeRequestFactoryMock.getFunctionInvokeRequest(capture(address), eq(ClientEntity.class))).andReturn(functionInvokeRequest);

        ODataClient clientMock = createMockBuilder(ODataClientImpl.class)
                .addMockedMethod("getInvokeRequestFactory")
                .addMockedMethod("getConfiguration").createMock();
        expect(clientMock.getInvokeRequestFactory()).andReturn(invokeRequestFactoryMock);
        expect(clientMock.getConfiguration()).andReturn(getConfiguration());

        replay(functionInvokeRequest, invokeRequestFactoryMock, clientMock);

        return clientMock;
    }
}
