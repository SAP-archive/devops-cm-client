package sap.ai.st.cm.plugins.ciintegration.odataclient;

import com.sap.cmclient.Transport;

/**
 * Data transfer object representing a transport.
 */
public class CMODataTransport implements Transport {

    private final String transportID;
    private final Boolean isModifiable;
    private final String description;
    private final String owner;

    public String getTransportID() {
        return transportID;
    }

    public Boolean isModifiable() {
        return isModifiable;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public CMODataTransport(String transportID, Boolean isModifiable, String description, String owner) {

        this.transportID = transportID;
        this.isModifiable = isModifiable;
        this.description = description;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "CMODataTransport [TransportID='" + transportID + "', IsModifiable='" + isModifiable + "', Description='"
                + description + "', Owner='" + owner + "']";
    }
}
