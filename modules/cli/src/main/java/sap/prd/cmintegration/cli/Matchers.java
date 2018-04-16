package sap.prd.cmintegration.cli;

import static java.lang.String.format;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class Matchers {

    private  Matchers() {
    }

    public static class ExitCodeMatcher extends BaseMatcher<ExitException> {

        private final int expected;
        private int actual = -1;

        ExitCodeMatcher(int exitCode) {
            expected = exitCode;
        }

        @Override
        public boolean matches(Object item) {
            if(! (item instanceof ExitException)) {
                return false;
            }
            actual = ((ExitException)item).getExitCode();
            return actual == expected;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(format("Unexpected exit code received: '%d'. Expected was: '%d'.", actual, expected));
        }
    }
    
    public final static ExitCodeMatcher exitCode(int exitCode) {
        return new ExitCodeMatcher(exitCode);
    }
}
