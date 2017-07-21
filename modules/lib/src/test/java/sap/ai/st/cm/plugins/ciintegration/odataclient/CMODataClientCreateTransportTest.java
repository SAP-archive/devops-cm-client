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
import static org.junit.Assert.assertThat;
import static sap.ai.st.cm.plugins.ciintegration.odataclient.MockHelper.getConfiguration;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.invoke.InvokeRequestFactory;
import org.apache.olingo.client.api.communication.request.invoke.ODataInvokeRequest;
import org.apache.olingo.client.api.communication.response.ODataInvokeResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.core.ODataClientImpl;
import org.apache.olingo.client.core.domain.ClientEntityImpl;
import org.apache.olingo.client.core.domain.ClientObjectFactoryImpl;
import org.apache.olingo.client.core.domain.ClientPropertyImpl;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CMODataClientCreateTransportTest extends CMODataClientBaseTest {

    Capture<String> contentType = null;

    @Before
    public void setup() throws Exception {
        super.setup();
        contentType = Capture.newInstance();
    }

    @After
    public void tearDown() throws Exception {
        contentType = null;
        super.tearDown();
    }

    @Test
    public void testCreateTransportRequestStraightForward() throws Exception {

        /*
         * 8000038673 holds already an open transport.
         * in case there is already an open transport the open transport is returned.
         */

        /*
         *  Comment line below and the captures later on in order to run against
         *  real back-end.
         */
        setMock(examinee, setupStraightForwardMock());

        CMODataTransport transport = examinee.createDevelopmentTransport("8000038673");

        assertThat(contentType.getValue(), is(equalTo("application/atom+xml")));
        assertThat(address.getValue().toASCIIString(),
            is(equalTo("https://example.org/endpoint/createTransport?ChangeID='8000038673'")));
        assertThat(transport.getTransportID(), is(equalTo("L21K90002H")));
        assertThat(transport.isModifiable(), is(equalTo(true)));
    }

    @Test
    public void testCreateTransportRequestForNotExistingChangeDocument() throws Exception {

        /*
         *  Comment line below and the captures later on in order to run against
         *  real back-end.
         */
        setMock(examinee, setupMock(new ODataClientErrorException(StatusLines.BAD_REQUEST)));

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("400"); // TODO 404 would be better ...

        try {
            examinee.createDevelopmentTransport("DOES_NOT_EXIST");
        } catch(Exception e) {
            assertThat(
                    address.getValue().toASCIIString(),
                    is(equalTo("https://example.org/endpoint/createTransport?ChangeID='DOES_NOT_EXIST'")));
            assertThat(contentType.getValue(), is(equalTo("application/atom+xml")));
            throw e;
        }
    }

    @Test
    public void testCreateTransportOnClosedClient() throws Exception{
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This instance of CMODataClient has been closed");
        examinee.close();
        examinee.createDevelopmentTransport("xx");
    }

    private ODataClient setupStraightForwardMock() {
        return setupMock(null);
    }

    @SuppressWarnings("unchecked")
    private ODataClient setupMock(ODataClientErrorException e) {

        class MockHelpers {
            ClientEntity getClientEntity() {

                ClientObjectFactoryImpl factory = new ClientObjectFactoryImpl();

                ClientEntity clientEntity = new ClientEntityImpl(new FullQualifiedName("AI_CRM_GW_CM_CI_SRV.Change"));

                clientEntity.getProperties().add(new ClientPropertyImpl("TransportID",
                        factory.newPrimitiveValueBuilder().setValue("L21K90002H").build()));

                clientEntity.getProperties().add(new ClientPropertyImpl("IsModifiable",
                        factory.newPrimitiveValueBuilder().setValue("true").build()));

                clientEntity.getProperties().add(new ClientPropertyImpl("Owner",
                        factory.newPrimitiveValueBuilder().setValue("me").build()));

                clientEntity.getProperties().add(new ClientPropertyImpl("Description",
                        factory.newPrimitiveValueBuilder().setValue("Lorem ipsum").build()));

                return clientEntity;
            }

            ODataInvokeResponse<ClientEntity> setupResponseMock() {

                ODataInvokeResponse<ClientEntity> responseMock = createMock(ODataInvokeResponse.class);
                expect(responseMock.getStatusCode()).andReturn(200);
                expect(responseMock.getBody()).andReturn(getClientEntity());
                responseMock.close();
                expectLastCall();
                replay(responseMock);
                return responseMock;
            }
        }

        ODataInvokeRequest<ClientEntity> functionInvokeRequest = createMock(ODataInvokeRequest.class);
        expect(functionInvokeRequest.setAccept(capture(contentType))).andReturn(functionInvokeRequest);

        if(e == null) {
            expect(functionInvokeRequest.execute()).andReturn(new MockHelpers().setupResponseMock());
        } else {
            expect(functionInvokeRequest.execute()).andThrow(e);
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
