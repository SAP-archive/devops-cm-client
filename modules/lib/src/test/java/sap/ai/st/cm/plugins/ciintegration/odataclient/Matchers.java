package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static java.lang.String.format;

import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.http.HttpClientException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class Matchers {

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

    private static class RootCauseMatcher extends BaseMatcher<Exception> {

        private final Class<? extends Exception> expectedRootCause;
        private Throwable actualRootCause = null;

        private RootCauseMatcher(Class<? extends Exception> expectedRootCause) {
            this.expectedRootCause = expectedRootCause;
        }

        @Override
        public boolean matches(Object o) {
            actualRootCause = getRootCause((Exception)o);
            return expectedRootCause.isAssignableFrom(actualRootCause.getClass());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(
                format("Root cause found '%s' does not match the expected root cause '%s'.",
                        (actualRootCause != null ? actualRootCause.getClass().getName() : "<n/a>"), expectedRootCause));
        }
    }

    private Matchers() {
    }

    static Matcher<Exception> hasRootCause(Class<? extends Exception> root) {
        return new RootCauseMatcher(root);
    }

    static Matcher<HttpClientException> carriesStatusCode(int statusCode) {
        return new RootCauseStatusCodeMatcher(statusCode);
    }

    private static Throwable getRootCause(Throwable thr) {
        while(thr.getCause() != null) {
            thr = thr.getCause();
        };
        return thr;
    }
}
