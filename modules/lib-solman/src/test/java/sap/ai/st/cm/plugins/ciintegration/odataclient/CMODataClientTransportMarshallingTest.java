package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.core.domain.ClientEntityImpl;
import org.apache.olingo.client.core.domain.ClientObjectFactoryImpl;
import org.apache.olingo.client.core.domain.ClientPropertyImpl;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CMODataClientTransportMarshallingTest {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testMarshallTransportwithoutTransportIdFails() {

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Transport id found to be null or empty");

        ClientEntity transport = new ClientEntityImpl(new FullQualifiedName("AI_CRM_GW_CM_CI_SRV.Change"));
        CMODataSolmanClient.toTransport("x", transport);
    }

    @Test
    public void testMarshallTransportWithoutModifiableFlaFail() {

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Modifiable flag found to be null or empty");

        ClientEntity transport = new ClientEntityImpl(new FullQualifiedName("AI_CRM_GW_CM_CI_SRV.Change"));
        transport.getProperties().add(new ClientPropertyImpl("TransportID", new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("8000038673").build()));
        CMODataSolmanClient.toTransport("x", transport);
    }

    @Test
    public void testMarshallTransportWithoutDescriptionSucceeds() {

        ClientEntity transportEnity = new ClientEntityImpl(new FullQualifiedName("AI_CRM_GW_CM_CI_SRV.Change"));
        List<ClientProperty> props = transportEnity.getProperties();
        props.add(new ClientPropertyImpl("TransportID", new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("8000038673").build()));
        props.add(new ClientPropertyImpl("IsModifiable", new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("true").build()));
        CMODataTransport transport = CMODataSolmanClient.toTransport("x", transportEnity);
        assertThat(transport.getDescription(), is(nullValue()));
    }

    @Test
    public void testMarshallTransportWithoutOwnerSucceeds() {

        ClientEntity transportEnity = new ClientEntityImpl(new FullQualifiedName("AI_CRM_GW_CM_CI_SRV.Change"));
        List<ClientProperty> props = transportEnity.getProperties();
        props.add(new ClientPropertyImpl("TransportID", new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("8000038673").build()));
        props.add(new ClientPropertyImpl("IsModifiable", new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("true").build()));
        CMODataTransport transport = CMODataSolmanClient.toTransport("x", transportEnity);
        assertThat(transport.getOwner(), is(nullValue()));
    }

    @Test
    public void testAllMembersMarshalled() {

        ClientEntity transportEnity = new ClientEntityImpl(new FullQualifiedName("AI_CRM_GW_CM_CI_SRV.Change"));
        List<ClientProperty> props = transportEnity.getProperties();
        props.add(new ClientPropertyImpl("TransportID", new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("8000038673").build()));
        props.add(new ClientPropertyImpl("IsModifiable", new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("true").build()));
        props.add(new ClientPropertyImpl("Owner", new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("me").build()));
        props.add(new ClientPropertyImpl("Description", new ClientObjectFactoryImpl().newPrimitiveValueBuilder().setValue("Lorem ipsum").build()));

        CMODataTransport transport = CMODataSolmanClient.toTransport("x", transportEnity);

        assertThat(transport.getTransportID(), is(equalTo("8000038673")));
        assertThat(transport.isModifiable(), is(equalTo(true)));
        assertThat(transport.getOwner(), is(equalTo("me")));
        assertThat(transport.getDescription(), is(equalTo("Lorem ipsum")));
    }

    
}
