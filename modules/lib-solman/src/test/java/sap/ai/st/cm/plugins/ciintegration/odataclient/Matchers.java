package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static java.lang.String.format;

import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.http.HttpClientException;
import org.apache.olingo.commons.api.ex.ODataError;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class Matchers {

    private static class ErrorMessageMatcher extends BaseMatcher<Exception> {

        String actualErrorMessage = "<n/a>", expected;

        private ErrorMessageMatcher(String substring) {
            if(substring == null) throw new NullPointerException();
            this.expected = substring;
        }

        @Override
        public boolean matches(Object o) {

            Throwable rootCause = getRootCause((Exception)o);

            if(! (rootCause instanceof ODataClientErrorException))
                return false;

            ODataError error = ((ODataClientErrorException)rootCause).getODataError();

            if(error == null)
                return false;

            actualErrorMessage = error.getMessage();
            return actualErrorMessage != null && actualErrorMessage.contains(expected);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(
                format("Error message '%s' does not contain substring '%s'.",
                    actualErrorMessage, expected));
        }

    }

    private static class RootCauseStatusCodeMatcher extends BaseMatcher<HttpClientException> {

        private final int expectedStatusCode;
        private int actualStatusCode = -1;

        private RootCauseStatusCodeMatcher(int expectedStatusCode) {
            this.expectedStatusCode = expectedStatusCode;
        }

        @Override
        public boolean matches(Object o) {

            Throwable rootCause = getRootCause((Exception) o);
            if(! (rootCause instanceof ODataClientErrorException))
                return false;

            actualStatusCode = ((ODataClientErrorException)rootCause)
                    .getStatusLine().getStatusCode();

            return actualStatusCode == expectedStatusCode;
        }

        @Override
        public void describeTo(Description description) {
            if(actualStatusCode == -1)
                description.appendText("Cannot detect status code.");
            else
                description.appendText(format("Actual status code '%d' does not match expected status code '%d'.",
                        actualStatusCode, expectedStatusCode));
            
        }
    }

    private Matchers() {
    }

    static Matcher<HttpClientException> carriesStatusCode(int statusCode) {
        return new RootCauseStatusCodeMatcher(statusCode);
    }

    static Matcher<Exception> hasServerSideErrorMessage(String substring) {
        return new ErrorMessageMatcher(substring);
    }

    private static Throwable getRootCause(Throwable thr) {
        while(thr.getCause() != null) {
            thr = thr.getCause();
        };
        return thr;
    }
}
