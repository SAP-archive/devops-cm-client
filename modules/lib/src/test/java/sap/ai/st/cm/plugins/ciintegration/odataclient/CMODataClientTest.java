package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.net.URI;

import org.apache.olingo.client.api.Configuration;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntityRequest;
import org.apache.olingo.client.api.communication.request.retrieve.RetrieveRequestFactory;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.core.ConfigurationImpl;
import org.apache.olingo.client.core.ODataClientImpl;
import org.apache.olingo.client.core.domain.ClientEntityImpl;
import org.apache.olingo.client.core.domain.ClientObjectFactoryImpl;
import org.apache.olingo.client.core.domain.ClientPropertyImpl;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.easymock.Capture;
import org.junit.Test;

public class CMODataClientTest {

    @Test
    public void testChangeStraightForward() throws Exception {

        Capture<URI> address =  Capture.newInstance();
        Capture<String> contentType = Capture.newInstance();

        CMODataClient examinee = new CMODataClient(
                "https://example.org/endpoint",
                "john.doe",
                "openSesame");

        // comment line below for testing against real backend.
        // Assert for the captures below needs to be commented also in this case.
        setMock(examinee, setupMock(address, contentType));

        CMODataChange change = examinee.getChange("8000038673");

        assertThat(change.getStatus(), is(equalTo("E0002")));
        assertThat(change.getChangeID(), is(equalTo("8000038673")));
        assertThat(contentType.getValue(), is(equalTo("application/atom+xml")));
        assertThat(address.getValue().toASCIIString(),
          is(equalTo(
            "https://example.org/endpoint/Changes('8000038673')")));
    }

    @SuppressWarnings("unchecked")
    private ODataClient setupMock(Capture<URI> address, Capture<String> contentType) {

        ClientEntity clientEntity = new ClientEntityImpl(new FullQualifiedName("AI_CRM_GW_CM_CI_SRV.Change"));
        clientEntity.getProperties().add(
            new ClientPropertyImpl("Status",
              new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("E0002").build()));

        ODataRetrieveResponse<ClientEntity> responseMock = createMock(ODataRetrieveResponse.class);
        expect(responseMock.getBody()).andReturn(clientEntity);

        ODataEntityRequest<ClientEntity> oDataEntityRequestMock = createMock(ODataEntityRequest.class);
        expect(oDataEntityRequestMock.setAccept(capture(contentType))).andReturn(oDataEntityRequestMock);
        expect(oDataEntityRequestMock.execute()).andReturn(responseMock);

        RetrieveRequestFactory retrieveRequestFactoryMock = createMock(RetrieveRequestFactory.class);
        expect(retrieveRequestFactoryMock.getEntityRequest(capture(address))).andReturn(oDataEntityRequestMock);

        ODataClient clientMock = createMockBuilder(ODataClientImpl.class)
                .addMockedMethod("getConfiguration")
                .addMockedMethod("getRetrieveRequestFactory").createMock();

        Configuration config = new ConfigurationImpl();
        config.setKeyAsSegment(false); // with that we get .../Changes('<ChangeId>'), otherwise .../Changes/'<ChangeId>'
        expect(clientMock.getConfiguration()).andReturn(config);
        expect(clientMock.getRetrieveRequestFactory()).andReturn(retrieveRequestFactoryMock);

        replay(responseMock, oDataEntityRequestMock, retrieveRequestFactoryMock, clientMock);

        return clientMock;
    }

    private static void setMock(CMODataClient examinee, ODataClient mock) throws Exception {
        Field client = CMODataClient.class.getDeclaredField("client");
        client.setAccessible(true);
        client.set(examinee, mock);
        client.setAccessible(false);
    }
}
