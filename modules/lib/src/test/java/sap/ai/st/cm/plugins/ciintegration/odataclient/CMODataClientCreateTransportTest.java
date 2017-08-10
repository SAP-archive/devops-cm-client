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
import static sap.ai.st.cm.plugins.ciintegration.odataclient.Matchers.carriesStatusCode;
import static sap.ai.st.cm.plugins.ciintegration.odataclient.Matchers.hasServerSideErrorMessage;
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
import org.apache.olingo.commons.api.ex.ODataError;
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
         * 8000038673 holds already an open transport. That change is an urgent change.
         * For urgent changes no new transport is created in case there is already an open
         * transport. For urgent changes the already existing open change is returned in
         * this case.
         *
         * The urgent change here is used in order to avoid a footprint in case the test is
         * executed against the real back-end.
         */

        /*
         *  Comment line below and the captures later on in order to run against
         *  real back-end.
         */
        setMock(examinee, setupMock(SERVICE_USER, "Jenkins CI Test"));

        CMODataTransport transport = examinee.createDevelopmentTransport("8000038673");

        assertThat(contentType.getValue(), is(equalTo("application/atom+xml")));
        assertThat(address.getValue().toASCIIString(),
            is(equalTo(SERVICE_ENDPOINT + "createTransport?ChangeID='8000038673'")));
        assertThat(transport.getTransportID(), is(equalTo("L21K90002H")));
        assertThat(transport.isModifiable(), is(equalTo(true)));
        assertThat(transport.getOwner(), is(equalTo(SERVICE_USER)));
        assertThat(transport.getDescription(), is(equalTo("Jenkins CI Test")));
    }

    @Test
    public void testCreateTransportRequestWithDesciptionAndOwnerWithNonExistingOwner() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expect(carriesStatusCode(400));
        thrown.expect(Matchers.hasServerSideErrorMessage("User DOESNOTEXIST does not exist in the system (or locked)."));

        /*
         *  Comment line below and the captures later on in order to run against
         *  real back-end.
         */
        setMock(examinee, setupMock(new ODataClientErrorException(StatusLines.BAD_REQUEST,
            new ODataError().setMessage("User DOESNOTEXIST does not exist in the system (or locked)."))));

        examinee.createDevelopmentTransportAdvanced("8000042445", "myDescription", "doesNotExist");
    }

  @Test
  public void testCreateTransportRequestWithDescriptionAndOwnerStraightForward() throws Exception {

      /*
       *  Comment line below and the captures later on in order to run against
       *  real back-end.
       */
      setMock(examinee, setupMock(SERVICE_USER, "my Description"));

      // with that test we check also for blanks in the description ...
      CMODataTransport transport = examinee.createDevelopmentTransportAdvanced("8000042445", "my Description", SERVICE_USER);

      assertThat(contentType.getValue(), is(equalTo("application/atom+xml")));
      assertThat(address.getValue().toASCIIString(),
          is(equalTo(SERVICE_ENDPOINT + "createTransportAdvanced?ChangeID='8000042445'&Description='my%20Description'&Owner='" + SERVICE_USER + "'")));
      assertThat(transport.isModifiable(), is(equalTo(true)));
      assertThat(transport.getDescription(), is(equalTo("my Description")));
      assertThat(transport.getOwner(), is(equalTo(SERVICE_USER)));
  }

    @Test
    public void testCreateTransportRequestForNotExistingChangeDocument() throws Exception {

        /*
         *  Comment line below and the captures later on in order to run against
         *  real back-end.
         */
        setMock(examinee, setupMock(new ODataClientErrorException(StatusLines.BAD_REQUEST,
                new ODataError().setMessage("DOES_NOT_E not found."))));

        thrown.expect(ODataClientErrorException.class);
        thrown.expect(carriesStatusCode(400)); // TODO 404 would be better ...
        thrown.expect(hasServerSideErrorMessage("DOES_NOT_E not found."));

        try {
            examinee.createDevelopmentTransport("DOES_NOT_EXIST");
        } catch(Exception e) {
            assertThat(
                    address.getValue().toASCIIString(),
                    is(equalTo(SERVICE_ENDPOINT + "createTransport?ChangeID='DOES_NOT_EXIST'")));
            assertThat(contentType.getValue(), is(equalTo("application/atom+xml")));
            throw e;
        }
    }

    @Test
    public void testCreateTransportOnClosedClient() throws Exception{
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("has been closed");
        examinee.close();
        examinee.createDevelopmentTransport("xx");
    }

    private ODataClient setupMock(String owner, String description) {
        return setupMock(owner, description, null);
    }

    private ODataClient setupMock(ODataClientErrorException e) {
        return setupMock(null, null, e);
    }

    @SuppressWarnings("unchecked")
    private ODataClient setupMock(final String owner, final String description, ODataClientErrorException e) {

        class MockHelpers {
            ClientEntity getClientEntity() {

                ClientObjectFactoryImpl factory = new ClientObjectFactoryImpl();

                ClientEntity clientEntity = new ClientEntityImpl(new FullQualifiedName("AI_CRM_GW_CM_CI_SRV.Change"));

                clientEntity.getProperties().add(new ClientPropertyImpl("TransportID",
                        factory.newPrimitiveValueBuilder().setValue("L21K90002H").build()));

                clientEntity.getProperties().add(new ClientPropertyImpl("IsModifiable",
                        factory.newPrimitiveValueBuilder().setValue("true").build()));

                clientEntity.getProperties().add(new ClientPropertyImpl("Owner",
                        factory.newPrimitiveValueBuilder().setValue(owner).build()));

                clientEntity.getProperties().add(new ClientPropertyImpl("Description",
                        factory.newPrimitiveValueBuilder().setValue(description).build()));

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
