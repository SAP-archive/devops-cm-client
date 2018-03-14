package com.sap.cmclient.http;

import java.net.URI;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import com.sap.cmclient.dto.Transport;

public class TransportRequestBuilder
{

  private final URI endpoint;
  private static final String SEPARATOR = "/";
  private static final String TRANSPORTS = "Transports";

  public TransportRequestBuilder(URI endpoint)
  {
    this.endpoint = endpoint;
  }

  public HttpGet getTransport(String id)
  {
    HttpGet get = new HttpGet(createTransportUri(id, null));
    return get;
  }

  public HttpPost createTransport(Transport trans)
  {
    return null;

  }

  public HttpPut updateTransport(String id)
  {
    
    HttpPut put = new HttpPut(createTransportUri(id,null));
    put.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    return put;

  }

  public HttpDelete deleteTransport(String id)
  {
    return null;

  }

  private String createTransportUri(String id, String expand)
  {
    final StringBuilder absolutUri = new StringBuilder(endpoint.toASCIIString()).append(SEPARATOR)
      .append(TRANSPORTS);
    if (id != null) {
      absolutUri.append("('").append(id).append("')");
    }
    if (expand != null) {
      absolutUri.append("/?$expand=").append(expand);
    }
    return absolutUri.toString();
  }

  public static String getEntityKey()
  {
    return TRANSPORTS;
  }

}
