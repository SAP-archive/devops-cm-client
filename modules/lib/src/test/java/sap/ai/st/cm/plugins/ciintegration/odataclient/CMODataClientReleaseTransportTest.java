package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.apache.http.message.BasicStatusLine;
import org.apache.olingo.client.api.Configuration;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.invoke.InvokeRequestFactory;
import org.apache.olingo.client.api.communication.request.invoke.ODataInvokeRequest;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.core.ConfigurationImpl;
import org.apache.olingo.client.core.ODataClientImpl;
import org.easymock.Capture;
import org.junit.After;
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
    public void testReleaseTransportFailsDueTransportHasAlreadyBeenReleased() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("400"); // TODO: 404 would be better

        setMock(examinee, setupExceptionMock());
        examinee.releaseDevelopmentTransport("8000038673", "L21K900026");
    }

    @Test
    public void testReleaseTransportFailsDueToNotExistingChange() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("400"); // TODO: 404 would be better

        setMock(examinee, setupExceptionMock());
        examinee.releaseDevelopmentTransport("CHANGE_ID_DOES_NOT_EXIST", "TRANSPORT_REQUEST_DOES_ALSO_NOT_EXIST");
    }

    @Test
    public void testReleaseTransportFailsDueToNotExistingTransport() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("400"); // TODO: 404 would be better

        setMock(examinee, setupExceptionMock());

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

    @SuppressWarnings("unchecked")
    private ODataClient setupExceptionMock() {

        ODataInvokeRequest<ClientEntity> functionInvokeRequest = createMock(ODataInvokeRequest.class);
        expect(functionInvokeRequest.setAccept(capture(contentType))).andReturn(functionInvokeRequest);
        expect(functionInvokeRequest.execute())
            .andThrow(new ODataClientErrorException(
                new BasicStatusLine(HTTP_1_1, 400, "Bad request.")));

        InvokeRequestFactory invokeRequestFactoryMock = createMock(InvokeRequestFactory.class);
        expect(invokeRequestFactoryMock.getFunctionInvokeRequest(capture(address), eq(ClientEntity.class))).andReturn(functionInvokeRequest);

        Configuration config = new ConfigurationImpl();
        config.setKeyAsSegment(false); // with that we get .../Changes('<ChangeId>'), otherwise .../Changes/'<ChangeId>'

        ODataClient clientMock = createMockBuilder(ODataClientImpl.class)
                .addMockedMethod("getInvokeRequestFactory")
                .addMockedMethod("getConfiguration").createMock();
        expect(clientMock.getInvokeRequestFactory()).andReturn(invokeRequestFactoryMock);
        expect(clientMock.getConfiguration()).andReturn(config);

        replay(functionInvokeRequest, invokeRequestFactoryMock, clientMock);

        return clientMock;
    }
}
