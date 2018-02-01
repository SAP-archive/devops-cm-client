package sap.prd.cmintegration.cli;

/**
 * Root for all exceptions related to the command line client in the
 * narrower sense.
 */
class CMCommandLineException extends RuntimeException {

    private static final long serialVersionUID = 5251372712902531523L;

    CMCommandLineException() {
        this((String)null);
    }

    CMCommandLineException(String message) {
        this(message, null);
    }

    CMCommandLineException(Throwable cause) {
        this(null, cause);
    }

    CMCommandLineException(String message, Throwable cause) {
        super(message, cause);
    }
}
