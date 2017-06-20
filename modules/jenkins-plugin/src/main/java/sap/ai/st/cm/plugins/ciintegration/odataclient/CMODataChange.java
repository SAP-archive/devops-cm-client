package sap.ai.st.cm.plugins.ciintegration.odataclient;

public class CMODataChange {
    
    private final String ChangeID;

    public String getChangeID() {
        return ChangeID;
    }

    public String getStatus() {
        return Status;
    }
    private final String Status;
    
    public CMODataChange(String ChangeID, String Status){
        
        this.ChangeID = ChangeID;
        this.Status = Status;
        
    }
    
}
