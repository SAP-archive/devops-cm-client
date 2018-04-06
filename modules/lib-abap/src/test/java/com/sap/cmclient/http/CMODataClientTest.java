package com.sap.cmclient.http;

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
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
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.sap.cmclient.dto.Transport;

// In order to run it with wire logging use:
// -Dorg.slf4j.simpleLogger.defaultLogLevel=trace
// -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog
// -Dorg.apache.commons.logging.simplelog.showdatetime=true
// -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG
// -Dorg.apache.commons.logging.simplelog.log.org.apache.http.wire=DEBUG

public class CMODataClientTest extends RecordableTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected CMODataAbapClient examinee;

    @Before
    public void setup() throws URISyntaxException {
        examinee = new CMODataAbapClient(getWiremockProxy(), 
                                     getUser(),
                                     getPassword());
    }

    @Test
    public void getTransportSucceedsTest() throws Exception {

        Transport transport = examinee.getTransport("A5DK900014");
        assertThat(transport.getTransportID(), is(equalTo("A5DK900014")));
        assertThat(transport.getOwner(), is(equalTo("ODATA")));
        assertThat(transport.getTargetSystem(), is(equalTo("A5T")));
        assertThat(transport.getStatus(), is(equalTo("D")));
        assertThat(transport.getType(), is(equalTo("W")));
    }

    @Test
    public void getTransportFailsDueToCutOfResponseTest() throws Exception {

        thrown.expect(EntityProviderException.class);
        thrown.expectMessage("An exception of type 'XMLStreamException' occurred");

        examinee.getTransport("A5DK900015");
    }

    @Test
    public void getTransportReturnsNullForNotExistingTransportTest() throws Exception {

        Transport transport = examinee.getTransport("A5DK900044");
        assertThat(transport, is(nullValue()));


    }

    @Test
    public void getTransportWithWrongCredentialsThrowsExceptionIndicating401Test() throws Exception {

        thrown.expect(new ResponseCodeMatcher(SC_UNAUTHORIZED));
        new CMODataAbapClient(getWiremockProxy(), "BAD_USER", "BAD_PASSWORD").getTransport("A5DK900014");
    }

    @Test
    public void uploadTest() throws UnexpectedHttpResponseException, IOException, CMODataClientException {

        String location = null;
        try(InputStream content = new ByteArrayInputStream("Hello SAP".getBytes(Charset.forName("UTF-8")))) {
            location = examinee.upload("A5DK900014", content);
        }

        assertThat(location, is(equalTo("http://example.org:8000/sap/opu/odata/SAP/SCTS_CLOUD_API_ODATA_SRV/TransportFiles(TransportId='A5DK900014',FileID='0050560313541ED88A89AB36F489B41E')")));
    }

    @Test
    public void createTransportTest() throws UnexpectedHttpResponseException, IOException, URISyntaxException, ODataException {
        
        Map<String, Object> transport = Transport.getTransportCreationRequestMap("ODATA", "my transport", "A5T", "", "W");
        Transport created = examinee.createTransport(transport);
        assertThat(created, is(not(nullValue())));
        assertThat(created.getTransportID(), is(not(nullValue())));
        assertThat(created.getTransportID().trim(), is(not("")));
        assertThat(created.getOwner(), is(equalTo("ODATA")));
        assertThat(created.getTargetSystem(), is(equalTo("A5T")));
        assertThat(created.getDescription(), is(equalTo("my transport")));
        assertThat(created.getStatus(), is(equalTo("D")));
        assertThat(created.getType(), is(equalTo("W")));
        assertThat(created.getCloud(), is(equalTo("X")));
    }

    @Test
    public void deleteTransportSucceedsTest() throws EntityProviderException, UnexpectedHttpResponseException, IOException {
        examinee.deleteTransport("A5DK900042");
    }

    @Test
    public void deleteTransportWhichDoesNotExistsFailsTest() throws EntityProviderException, UnexpectedHttpResponseException, IOException {

        // actually 404 would be more appropriate, but 500 is returned from server...
        thrown.expect(new ResponseCodeMatcher(SC_INTERNAL_SERVER_ERROR));
        examinee.deleteTransport("A5DK900044");
    }

    @Ignore("No test captured yet")
    @Test
    public void releaseTransportTest() throws UnexpectedHttpResponseException, IOException, EntityProviderException, EdmException {
        Transport transport = examinee.releaseTransport("A5DK900082");
        System.out.println(transport);
    }

    @Ignore("No test captured yet")
    @Test
    public void importTransportTest() throws UnexpectedHttpResponseException, IOException {
        examinee.importTransport("A5D", "A5DK900057");
    }

    private static class ResponseCodeMatcher extends BaseMatcher<UnexpectedHttpResponseException> {

        private final int expected;
        private int received = -1;

        ResponseCodeMatcher(int expected) {
          this.expected = expected;
        }

        @Override
        public boolean matches(Object item) {
            if( ! (item instanceof UnexpectedHttpResponseException)) return false;
            this.received = ((UnexpectedHttpResponseException)item).getStatus().getStatusCode();
            return  received == expected;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(format("Should fail with status code '%d', but got other status code '%d'.", expected, received));
        }
    }
}
