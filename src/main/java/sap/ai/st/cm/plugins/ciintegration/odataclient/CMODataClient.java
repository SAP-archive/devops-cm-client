package sap.ai.st.cm.plugins.ciintegration.odataclient;

import java.net.URI;
import java.util.ArrayList;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntityRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import sap.ai.st.cm.plugins.ciintegration.CIIntegrationGlobalConfiguration;

public class CMODataClient {

    private final ODataClient client;
    private final CIIntegrationGlobalConfiguration configuration;

    public CMODataClient(CIIntegrationGlobalConfiguration configuration) {

        this.configuration = configuration;

        this.client = ODataClientFactory.getClient();
        this.client.getConfiguration().setHttpClientFactory(new BasicAuthHttpClientFactory(this.configuration.getServiceUser(), this.configuration.getServicePassword()));

    }

    public CMODataChange getChange(String ChangeID) throws Exception {

        URI entityUri = this.client.newURIBuilder(this.configuration.getServiceURL()).appendEntitySetSegment("Changes").appendKeySegment(ChangeID).build();

        ODataEntityRequest<ClientEntity> request = this.client.getRetrieveRequestFactory().getEntityRequest(entityUri);

        request.setAccept("application/atom+xml");

        ODataRetrieveResponse<ClientEntity> response = request.execute();

        return new CMODataChange(ChangeID, response.getBody().getProperty("Status").getValue().toString());

    }
    
    public ArrayList<CMODataTransport> getChangeTransports(String ChangeID) throws Exception {
        
        URI entityUri = this.client.newURIBuilder(this.configuration.getServiceURL()).appendEntitySetSegment("Changes"
        ).appendKeySegment(ChangeID).appendNavigationSegment("Transports").build();
        
        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = this.client.getRetrieveRequestFactory().getEntitySetIteratorRequest(entityUri);

        request.setAccept("application/atom+xml");

        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();
        
        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = response.getBody();
        
        ArrayList<CMODataTransport> transportList = new ArrayList<>();
        
        while (iterator.hasNext() ){
            
            ClientEntity transport = iterator.next();
            
            transportList.add(new CMODataTransport(transport.getProperty("TransportID").getValue().toString(), Boolean.parseBoolean(transport.getProperty("IsModifiable").getValue().toString())));
        }
        
        return transportList;        
    }
}
