package sap.ai.st.cm.plugins.ciintegration.odataclient;

/**
 * Root of the exception hierarchy for all exceptions specific
 * to the CMODataClient.
 */
public class CMODataClientException extends Exception {

    private static final long serialVersionUID = -404548464645983071L;

    public CMODataClientException() {
        this((String)null);
    }

    public CMODataClientException(String message) {
        this(message, null);
    }

    public CMODataClientException(Throwable cause) {
        this(null, cause);
    }

    public CMODataClientException(String message, Throwable cause) {
        this(message, cause, true, true);
    }

    public CMODataClientException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
