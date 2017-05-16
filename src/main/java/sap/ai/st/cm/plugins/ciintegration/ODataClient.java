package sap.ai.st.cm.plugins.ciintegration;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OClientBehaviors;
import org.odata4j.core.OEntity;

public class ODataClient {
    
    private final ODataConsumer consumer;
    
    public ODataClient(CIIntegrationGlobalConfiguration configuration){
        
        this.consumer = ODataConsumer.newBuilder(configuration.getServiceURL()).setClientBehaviors(OClientBehaviors.basicAuth(configuration.getServiceUser(), configuration.getServicePassword())).build();
        
    }
    
    public OEntity getChange(String ChangeID) {
        
        return consumer.getEntity("Changes", ChangeID).execute();
        
    }    
}
