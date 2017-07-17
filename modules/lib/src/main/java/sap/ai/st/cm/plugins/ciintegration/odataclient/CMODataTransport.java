package sap.ai.st.cm.plugins.ciintegration.odataclient;

public class CMODataTransport {

    private final String TransportID;
    private final Boolean IsModifiable;

    public String getTransportID() {
        return TransportID;
    }

    public Boolean isModifiable() {
        return IsModifiable;
    }

    public CMODataTransport(String TransportID, Boolean IsModifiable) {

        this.TransportID = TransportID;
        this.IsModifiable = IsModifiable;
    }

    @Override
    public String toString() {
        return "CMODataTransport [TransportID=" + TransportID + ", IsModifiable=" + IsModifiable + "]";
    }
}
