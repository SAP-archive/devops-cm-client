package com.sap.cmclient;

import static java.lang.String.format;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class Matchers {

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

    private static class RootCausMessageeMatcher extends BaseMatcher<Exception> {

        private final String expectedMessage;
        private String actualMessage = null;

        private RootCausMessageeMatcher(String expectedMessage) {
            this.expectedMessage = expectedMessage;
        }

        @Override
        public boolean matches(Object o) {
            actualMessage = getRootCause((Exception)o).getMessage();
            return (actualMessage == null && expectedMessage == null) || 
                    (actualMessage != null && actualMessage.contains(expectedMessage));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(
                format("Root cause message '%s' does not contain '%s'.",
                        (actualMessage != null ? actualMessage : "<n/a>"), expectedMessage));
        }
    }

    private Matchers() {
    }

    public static Matcher<Exception> hasRootCause(Class<? extends Exception> root) {
        return new RootCauseMatcher(root);
    }

    public static Matcher<Exception> rootCauseMessageContains(String expected) {
        return new RootCausMessageeMatcher(expected);
    }

    private static Throwable getRootCause(Throwable thr) {
        while(thr.getCause() != null) {
            thr = thr.getCause();
        };
        return thr;
    }
}
