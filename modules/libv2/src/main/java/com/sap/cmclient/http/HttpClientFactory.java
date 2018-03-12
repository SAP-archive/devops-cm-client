package com.sap.cmclient.http;


import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientFactory {

    private final CookieStore cookieStore;
    private final CredentialsProvider credentialsProvider;

    public HttpClientFactory(CookieStore cookieStore, CredentialsProvider credentialsProvider) {
        this.cookieStore = cookieStore;
        this.credentialsProvider = credentialsProvider;
    }

    HttpClient createClient() {

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultCookieStore(cookieStore);
        builder.setDefaultCredentialsProvider(credentialsProvider);
        builder.setUserAgent("TODO: Reasonable user agent string goes here.");
        return builder.build();
    }

}
