package sap.prd.cmintegration.cli;

public class TransportNotFoundException extends CMCommandLineException {

    private static final long serialVersionUID = 7378231344272008562L;
    private final String transportId;

    public TransportNotFoundException(String transportId) {
       this(transportId, (String)null);
    }

    public TransportNotFoundException(String transportId, String message) {
        this(transportId, message, null);
    }

    public TransportNotFoundException(String transportId, Throwable cause) {
        this(transportId, (String) null ,cause);
    }

    public TransportNotFoundException(String transportId, String message, Throwable cause) {
        super(message, cause);
        this.transportId = transportId;
    }

    public String getTransportId() {
        return transportId;
    }
}
