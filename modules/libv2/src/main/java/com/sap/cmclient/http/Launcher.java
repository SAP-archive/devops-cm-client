package com.sap.cmclient.http;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;

public class Launcher {

    // args[0] "http://wdflbmd16301.wdf.sap.corp:8000/sap/opu/odata/SAP/SCTS_CLOUD_API_ODATA_SRV/Transports(\'A5DK900014\')"
    // args[1] ODATA
    // args[2] <the password ...>
    
    // In order to run it with wire logging use:
    // -Dorg.slf4j.simpleLogger.defaultLogLevel=trace
    // -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog
    // -Dorg.apache.commons.logging.simplelog.showdatetime=true
    // -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG
    // -Dorg.apache.commons.logging.simplelog.log.org.apache.http.wire=DEBUG

    public final static void main(String[] args) throws Exception {

        URI uri = new URI(args[0]);

        // the same instance needs to be used as long as we are in the same session. Hence multiple
        // clients must share the same cookie store. Reason: we are logged on with the first request
        // and get a cookie. That cookie - expressing the user has already been logged in - needs to be
        // present in all subsequent requests.
        CookieStore sessionCookieStore = new BasicCookieStore();

        BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        basicCredentialsProvider.setCredentials(
                new AuthScope(uri.getHost(), uri.getPort()),
                new UsernamePasswordCredentials(args[1], args[2]));

        HttpClient client = new HttpClientFactory(sessionCookieStore, basicCredentialsProvider).createClient();

        HttpUriRequest get = new HttpGet(uri);
        HttpResponse response = client.execute(get);
        InputStream is = response.getEntity().getContent();

        System.out.println(response.getStatusLine());

        StringBuilder sb = new StringBuilder();
        int i = 0;
        while((i = is.read()) != -1) {
            sb.append((char) i );
        }

        System.out.println(sb.toString());

    }

}
