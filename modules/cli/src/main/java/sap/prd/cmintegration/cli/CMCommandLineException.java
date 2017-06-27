package sap.prd.cmintegration.cli;

public class CMCommandLineException extends RuntimeException {

    private static final long serialVersionUID = 5251372712902531523L;

    public CMCommandLineException() {
        this((String)null);
    }

    public CMCommandLineException(String message) {
        this(message, null);
    }

    public CMCommandLineException(Throwable cause) {
        this(null, cause);
    }

    public CMCommandLineException(String message, Throwable cause) {
        super(message, cause);
    }
}
