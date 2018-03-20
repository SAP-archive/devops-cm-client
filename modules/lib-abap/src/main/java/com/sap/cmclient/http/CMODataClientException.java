package com.sap.cmclient.http;

public class CMODataClientException extends Exception {

    private static final long serialVersionUID = 2238456847826920931L;

    public CMODataClientException() {
    }

    public CMODataClientException(String message) {
        super(message);
    }

    public CMODataClientException(Throwable cause) {
        super(cause);
    }

    public CMODataClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CMODataClientException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
