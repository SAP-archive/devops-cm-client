package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static org.apache.commons.lang3.StringUtils.join;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static sap.ai.st.cm.plugins.ciintegration.odataclient.Matchers.carriesStatusCode;
import static sap.ai.st.cm.plugins.ciintegration.odataclient.MockHelper.getConfiguration;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.request.retrieve.RetrieveRequestFactory;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.core.ODataClientImpl;
import org.apache.olingo.client.core.domain.ClientObjectFactoryImpl;
import org.apache.olingo.client.core.domain.ClientPropertyImpl;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class CMODataClientGetTransportsTest extends CMODataClientBaseTest {


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
        ArrayList<CMODataTransport> changeTransports = examinee.getChangeTransports("8000042445");

        assertThat(join(getTransportIds(changeTransports), " "), allOf(
                containsString("L21K90002J"),
                containsString("L21K90002L"),
                containsString("L21K90002N")));

        assertThat(changeTransports.get(0).getDescription(), is(equalTo("S 8000038673: HCP CI Jenkins Deploy UC 1")));
        assertThat(changeTransports.get(0).getOwner(), is(equalTo("john.doe")));

        assertThat(address.getValue().toASCIIString(),
                is(equalTo("https://example.org/endpoint/Changes('8000042445')/Transports")));
    }

    /*
     * No java 8 streams for the lib since the lib is also used from the jenkins plugin which
     * has (for some reasons (?)) a constrain to java 7.
     */
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
        thrown.expect(carriesStatusCode(400)); // TODO 404 would be better

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

    @Test
    public void testGetChangeTransportsOnClosedClient() throws Exception{
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This instance of CMODataClient has been closed");
        examinee.close();
        examinee.getChangeTransports("xx");
    }

    private ODataClient setupExceptionMock() throws Exception {
        return setupMock(new ODataClientErrorException(StatusLines.BAD_REQUEST));
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
            expect(iteratorMock.next()).andReturn(setupTransportMock("L21K90002N", false));
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(setupTransportMock("L21K90002L", false));
            expect(iteratorMock.hasNext()).andReturn(true);
            expect(iteratorMock.next()).andReturn(setupTransportMock("L21K90002J", true));
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

        ODataClient clientMock = createMockBuilder(ODataClientImpl.class)
                .addMockedMethod("getRetrieveRequestFactory")
                .addMockedMethod("getConfiguration").createMock();
        expect(clientMock.getRetrieveRequestFactory()).andReturn(retrieveRequestFactoryMock);
        expect(clientMock.getConfiguration()).andReturn(getConfiguration());
        replay(responseMock, oDataEntityRequestMock, retrieveRequestFactoryMock, clientMock);
        return clientMock;
    }

    private static ClientEntity setupTransportMock(String transportId, boolean isModifiable) {

        ClientEntity transportMock = createMock(ClientEntity.class);

        ClientProperty t = new ClientPropertyImpl("TransportID",
            new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue(transportId).build());

        ClientProperty m = new ClientPropertyImpl("IsModifiable",
            new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("true").build());

        ClientProperty d = new ClientPropertyImpl("Description",
                new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("S 8000038673: HCP CI Jenkins Deploy UC 1").build());

        ClientProperty o = new ClientPropertyImpl("Owner",
                new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("john.doe").build());

        expect(transportMock.getProperty("TransportID")).andReturn(t);
        expect(transportMock.getProperty("IsModifiable")).andReturn(m);
        expect(transportMock.getProperty("Description")).andReturn(d);
        expect(transportMock.getProperty("Owner")).andReturn(o);

        replay(transportMock);
        return transportMock;
    }
}
