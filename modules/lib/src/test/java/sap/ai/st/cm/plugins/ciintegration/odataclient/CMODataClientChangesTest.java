package sap.ai.st.cm.plugins.ciintegration.odataclient;

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
import static sap.ai.st.cm.plugins.ciintegration.odataclient.MockHelper.getConfiguration;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntityRequest;
import org.apache.olingo.client.api.communication.request.retrieve.RetrieveRequestFactory;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
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

public class CMODataClientChangesTest extends CMODataClientBaseTest {

    private Capture<String> contentType;

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
    public void testChangeStraightForward() throws Exception {

        // comment line below for testing against real backend.
        // Assert for the captures below needs to be commented also in this case.
        setMock(examinee, setupStraightForwardMock());

        CMODataChange change = examinee.getChange("8000038673");

        assertThat(change.isInDevelopment(), is(equalTo(true)));
        assertThat(change.getChangeID(), is(equalTo("8000038673")));
        assertThat(contentType.getValue(), is(equalTo("application/atom+xml")));
        assertThat(address.getValue().toASCIIString(),
          is(equalTo(
            "https://example.org/endpoint/Changes('8000038673')")));
    }

    @Test
    public void testChangeDoesNotExist() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expect(carriesStatusCode(404));

        // comment line below for testing against real backend.
        // Assert for the captures below needs to be commented also in this case.
        setMock(examinee, setupChangeDoesNotExistMock());

        examinee.getChange("001");
    }

    @Test
    public void testChangeBadCredentials() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expect(carriesStatusCode(401));

        CMODataClient examinee = new CMODataClient(
                "https://example.org/endpoint",
                "NOBODY",
                "openSesame");

        // comment line below for testing against real backend.
        // Assert for the captures below needs to be commented also in this case.
        setMock(examinee, setupBadCredentialsMock());

        examinee.getChange("8000038673");
    }

    @Test
    public void testGetChangeOnClosedClient() throws Exception{
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This instance of CMODataClient has been closed");
        examinee.close();
        examinee.getChange("xx");
    }

    private ODataClient setupStraightForwardMock() {
        return setupMock(null);
    }

    private ODataClient setupBadCredentialsMock() {
        return setupMock(
                new ODataClientErrorException(
                        StatusLines.UNAUTHORIZED));
    }

    private ODataClient setupChangeDoesNotExistMock() {
        return setupMock(
                new ODataClientErrorException(
                        StatusLines.NOT_FOUND));
    }

    @SuppressWarnings("unchecked")
    private ODataClient setupMock(ODataClientErrorException e) {

        class MockHelpers {

            private ClientEntity setupClientEntityMock() {

                ClientEntity clientEntity = new ClientEntityImpl(new FullQualifiedName("AI_CRM_GW_CM_CI_SRV.Change"));

                clientEntity.getProperties().add(
                        new ClientPropertyImpl("ChangeID",
                          new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("8000038673").build()));

                clientEntity.getProperties().add(
                        new ClientPropertyImpl("IsInDevelopment",
                          new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("true").build()));

                return clientEntity;
            }

            ODataRetrieveResponse<ClientEntity> setupResponseMock() {
                ODataRetrieveResponse<ClientEntity> responseMock = createMock(ODataRetrieveResponse.class);
                expect(responseMock.getBody()).andReturn(setupClientEntityMock());
                responseMock.close();
                expectLastCall().once();
                replay(responseMock);
                return responseMock;
            }
        }

        ODataEntityRequest<ClientEntity> oDataEntityRequestMock = createMock(ODataEntityRequest.class);
        expect(oDataEntityRequestMock.setAccept(capture(contentType))).andReturn(oDataEntityRequestMock);

        if(e != null) {
            expect(oDataEntityRequestMock.execute()).andThrow(e);
        } else {
            expect(oDataEntityRequestMock.execute()).andReturn(new MockHelpers().setupResponseMock());
        }

        RetrieveRequestFactory retrieveRequestFactoryMock = createMock(RetrieveRequestFactory.class);
        expect(retrieveRequestFactoryMock.getEntityRequest(capture(address))).andReturn(oDataEntityRequestMock);

        ODataClient clientMock = createMockBuilder(ODataClientImpl.class)
                .addMockedMethod("getConfiguration")
                .addMockedMethod("getRetrieveRequestFactory").createMock();

        expect(clientMock.getConfiguration()).andReturn(getConfiguration());
        expect(clientMock.getRetrieveRequestFactory()).andReturn(retrieveRequestFactoryMock);

        replay(oDataEntityRequestMock, retrieveRequestFactoryMock, clientMock);

        return clientMock;
    }
}
