package com.sap.cmclient.http;

import static java.lang.String.format;

import org.apache.http.StatusLine;

public class UnexpectedHttpResponseException extends CMODataClientException {


    private static final long serialVersionUID = -5925875459977637067L;

    private final StatusLine status;

    public UnexpectedHttpResponseException(StatusLine status) {
        this(format("Unexpected http response code: '%s'.", 
                status == null ? "<n/a>" : status.getStatusCode()), status);
    }

    public UnexpectedHttpResponseException(String message, StatusLine status) {
        super(message);
        this.status = status;
    }

    public StatusLine getStatus() {
        return status;
    }
}
