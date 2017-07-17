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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.message.BasicStatusLine;
import org.apache.olingo.client.api.Configuration;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.request.retrieve.RetrieveRequestFactory;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.core.ConfigurationImpl;
import org.apache.olingo.client.core.ODataClientImpl;
import org.apache.olingo.client.core.domain.ClientObjectFactoryImpl;
import org.apache.olingo.client.core.domain.ClientPropertyImpl;
import org.easymock.Capture;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class CMODataClientTransportsTest extends CMODataClientBaseTest {


    private Capture<String> contentType = Capture.newInstance();
    @Before
    public void setup() throws Exception {
        super.setup();
    }

    @After
    public void tearDown() throws Exception{
        super.tearDown();
    }

    @Test
    public void testGetTransportsStraightForward() throws Exception {

        setMock(examinee, setupStraightForwardMock());
        ArrayList<CMODataTransport> changeTransports = examinee.getChangeTransports("8000038673");

        assertThat(getTransportIds(changeTransports), Matchers.containsInAnyOrder(
                "L21K90002B", "L21K90002A", "L21K900026",
                "L21K900028", "L21K900029", "L21K90002C",
                "L21K90002D", "L21K90002E", "L21K90002H"));
        assertThat(changeTransports.size(), is(equalTo(9)));

        assertThat(address.getValue().toASCIIString(),
                is(equalTo("https://example.org/endpoint/Changes('8000038673')/Transports")));
    }

    private static Collection<String> getTransportIds(Collection<CMODataTransport> transports) {
        Collection<String> transportIds = Sets.newHashSet();
        for(CMODataTransport t : transports) {
            transportIds.add(t.getTransportID());
        }
        return transportIds;
    }

    @Test
    public void testGetTransportsChangeIdDoesNotExist() throws Exception {

        thrown.expect(ODataClientErrorException.class);
        thrown.expectMessage("400"); // TODO 404 would be better

        setMock(examinee, setupExceptionMock());

        try {
            examinee.getChangeTransports("DOES_NOT_EXIST");
        } catch(Exception e) {
            assertThat(address.getValue().toASCIIString(),
                is(equalTo("https://example.org/endpoint/Changes('DOES_NOT_EXIST')/Transports")));
            throw e;
        }
    }

    @Test
    public void testGetTransportsChangeIdNotProvided() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ChangeID was null or empty");

        setMock(examinee, setupStraightForwardMock());

        examinee.getChangeTransports(null);
    }

    private ODataClient setupExceptionMock() throws Exception {
        return setupMock(new ODataClientErrorException(new BasicStatusLine(HTTP_1_1, 400, "Bad Request.")));
    }

    private ODataClient setupStraightForwardMock() throws Exception {
        return setupMock(null);
    }

    @SuppressWarnings("unchecked")
    private ODataClient setupMock(Exception e) {

        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> responseMock = createMock(ODataRetrieveResponse.class);

        if(e != null) {
            expect(responseMock.getBody()).andThrow(e);
        } else {

            ClientEntitySetIterator<ClientEntitySet, ClientEntity> iteratorMock = createMock(ClientEntitySetIterator.class);
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(createTransportMock("L21K900026", false));
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(createTransportMock("L21K900028", false));
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(createTransportMock("L21K900029", false));
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(createTransportMock("L21K90002A", false));
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(createTransportMock("L21K90002B", false));
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(createTransportMock("L21K90002C", false));
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(createTransportMock("L21K90002D", false));
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(createTransportMock("L21K90002E", false));
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(createTransportMock("L21K90002H", true));

            expect(iteratorMock.hasNext()).andReturn(false);

            expect(responseMock.getBody()).andReturn(iteratorMock);
            replay(iteratorMock);
        }

        responseMock.close();
        expectLastCall();

        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> oDataEntityRequestMock = createMock(ODataEntitySetIteratorRequest.class);
        expect(oDataEntityRequestMock.setAccept(capture(contentType))).andReturn(oDataEntityRequestMock);
        expect(oDataEntityRequestMock.execute()).andReturn(responseMock);

        RetrieveRequestFactory retrieveRequestFactoryMock = createMock(RetrieveRequestFactory.class);
        expect(retrieveRequestFactoryMock.getEntitySetIteratorRequest(capture(address))).andReturn(oDataEntityRequestMock);

        Configuration config = new ConfigurationImpl();
        config.setKeyAsSegment(false); // with that we get .../Changes('<ChangeId>'), otherwise .../Changes/'<ChangeId>'

        ODataClient clientMock = createMockBuilder(ODataClientImpl.class)
                .addMockedMethod("getRetrieveRequestFactory")
                .addMockedMethod("getConfiguration").createMock();
        expect(clientMock.getRetrieveRequestFactory()).andReturn(retrieveRequestFactoryMock);
        expect(clientMock.getConfiguration()).andReturn(config);
        replay(responseMock, oDataEntityRequestMock, retrieveRequestFactoryMock, clientMock);
        return clientMock;
    }

    private ClientEntity createTransportMock(String transportId, boolean isModifiable) {

        ClientEntity transportMock = createMock(ClientEntity.class);

        ClientProperty t = new ClientPropertyImpl("TransportID",
            new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue(transportId).build());

        ClientProperty m = new ClientPropertyImpl("IsModifiable",
            new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("true").build());

        expect(transportMock.getProperty("TransportID")).andReturn(t);
        expect(transportMock.getProperty("IsModifiable")).andReturn(m);

        replay(transportMock);
        return transportMock;
    }
}
