package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Preconditions;
import com.sap.cmclient.Transport;

/**
 * Data transfer object representing a transport.
 */
public class CMODataTransport implements Transport {

    private final String transportID;
    private final String developmentSystemID;
    private final Boolean isModifiable;
    private final String description;
    private final String owner;

    public String getTransportID() {
        return transportID;
    }

    public String getDevelopmentSystemID() {
        return developmentSystemID;
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

    public CMODataTransport(String transportID, String developmentSystemID, Boolean isModifiable, String description, String owner) {

        Preconditions.checkArgument(! isNullOrEmpty(transportID), "transportId was null or empty.");
        this.transportID = transportID;
        this.developmentSystemID = developmentSystemID;
        this.isModifiable = isModifiable;
        this.description = description;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "CMODataTransport [TransportID='" + transportID + "', DevelopmentSystemID='" + developmentSystemID + "'IsModifiable='" + isModifiable + "', Description='"
                + description + "', Owner='" + owner + "']";
    }
}
