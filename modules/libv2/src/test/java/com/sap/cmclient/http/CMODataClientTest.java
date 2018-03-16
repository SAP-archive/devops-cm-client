package com.sap.cmclient.http;

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sap.cmclient.dto.Transport;

public class CMODataClientTest extends RecordableTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected CMODataClient examinee;

    @Before
    public void setup() throws URISyntaxException {
        examinee = new CMODataClient(getWiremockProxy(), 
                                     getUser(),
                                     getPassword());
    }

    @After
    public void tearDown() {
        if(! isRecording()) {
            WireMock.resetAllRequests();
            WireMock.resetAllScenarios();
        }
    }

    @Test
    public void getTransportSucceedsTest() throws Exception {

        Transport transport = examinee.getTransport("A5DK900014");
        assertThat(transport.getId(), is(equalTo("A5DK900014")));
        assertThat(transport.getOwner(), is(equalTo("ODATA")));
        assertThat(transport.getTargetSystem(), is(equalTo("A5T")));
        assertThat(transport.getStatus(), is(Transport.Status.D));
        assertThat(transport.getType(), is(Transport.Type.W));
    }

    @Test
    public void getTransportReturnsNullForNotExistingTransportTest() throws Exception {

        Transport transport = examinee.getTransport("DOES_NOT_EXIST");
        assertThat(transport, is(nullValue()));
    }

    @Test
    public void getTransportWithWrongCredentialsThrowsExceptionIndicating401Test() throws Exception {

        thrown.expect(new BaseMatcher<UnexpectedHttpResponseException>() {

            private final int expected = SC_UNAUTHORIZED;
            private int received = -1;

            @Override
            public boolean matches(Object item) {
                this.received = ((UnexpectedHttpResponseException)item).getStatus().getStatusCode();
                return  received == expected;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(format("Should fail with status code '%d', but got other status code '%d'.", expected, received));
            }
        });

        new CMODataClient(getWiremockProxy(), "BAD_USER", "BAD_PASSWORD").getTransport("A5DK900014");
    }

    @Test
    public void uploadTest() throws UnexpectedHttpResponseException, IOException, CMODataClientException {

        String location = null;
        try(InputStream content = new ByteArrayInputStream("Hello SAP".getBytes(Charset.forName("UTF-8")))) {
            location = examinee.upload("A5DK900014", content);
        }

        assertThat(location, is(equalTo("http://example.org:8000/sap/opu/odata/SAP/SCTS_CLOUD_API_ODATA_SRV/TransportFiles(TransportId='A5DK900014',FileID='0050560313541ED88A89AB36F489B41E')")));
    }
}
