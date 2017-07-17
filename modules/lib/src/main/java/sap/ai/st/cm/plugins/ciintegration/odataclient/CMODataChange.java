package sap.ai.st.cm.plugins.ciintegration.odataclient;

public class CMODataChange {
    
    private final String ChangeID;

    public String getChangeID() {
        return ChangeID;
    }

    public boolean isInDevelopment() {
        return isInDevelopment;
    }
    private final boolean isInDevelopment;
    
    public CMODataChange(String ChangeID, boolean isInDevelopment){
        
        this.ChangeID = ChangeID;
        this.isInDevelopment = isInDevelopment;
        
    }
    
}
