package sap.ai.st.cm.plugins.ciintegration.odataclient;

public class CMODataTransport {

    private final String TransportID;
    private final Boolean IsModifiable;
    private final String Description;
    private final String Owner;

    public String getTransportID() {
        return TransportID;
    }

    public Boolean isModifiable() {
        return IsModifiable;
    }

    public String getDescription() {
        return Description;
    }

    public String getOwner() {
        return Owner;
    }

    public CMODataTransport(String TransportID, Boolean IsModifiable, String Description, String Owner) {

        this.TransportID = TransportID;
        this.IsModifiable = IsModifiable;
        this.Description = Description;
        this.Owner = Owner;
    }

    @Override
    public String toString() {
        return "CMODataTransport [TransportID='" + TransportID + "', IsModifiable='" + IsModifiable + "', Description='"
                + Description + "', Owner='" + Owner + "']";
    }
}
