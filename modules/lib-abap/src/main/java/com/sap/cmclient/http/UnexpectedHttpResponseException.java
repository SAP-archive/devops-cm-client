package com.sap.cmclient.http;

import org.apache.http.StatusLine;

public class UnexpectedHttpResponseException extends CMODataClientException {


    private static final long serialVersionUID = -5925875459977637067L;

    private final StatusLine status;

    public UnexpectedHttpResponseException(StatusLine status) {
        this(null, status);
    }

    public UnexpectedHttpResponseException(String message, StatusLine status) {
        super(message);
        this.status = status;
    }
    
    public StatusLine getStatus() {
        return status;
    }
}
