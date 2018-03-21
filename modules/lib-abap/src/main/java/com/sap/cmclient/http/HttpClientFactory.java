package com.sap.cmclient.http;


import static com.sap.cmclient.VersionHelper.getOlingoV2Version;
import static com.sap.cmclient.VersionHelper.getShortVersion;
import static java.lang.String.format;

import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientFactory {

    private final CookieStore cookieStore;
    private final CredentialsProvider credentialsProvider;

    public HttpClientFactory(CookieStore cookieStore, CredentialsProvider credentialsProvider) {
        this.cookieStore = cookieStore;
        this.credentialsProvider = credentialsProvider;
    }

    CloseableHttpClient createClient() {

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultCookieStore(cookieStore);
        builder.setDefaultCredentialsProvider(credentialsProvider);
        builder.setUserAgent(format("SAP CM Client/%s based on Olingo v%s",
                getShortVersion(),
                getOlingoV2Version()));
        return builder.build();
    }
}

