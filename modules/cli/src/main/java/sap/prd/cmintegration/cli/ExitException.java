package sap.prd.cmintegration.cli;

/**
 * Used in case a status is encounter where any further processing inside
 * the command line client does not make sense. The exit code transported
 * alongside with this exception is used as exit code of the Java Virtual
 * Machine.
 */
class ExitException extends CMCommandLineException {

    private static final long serialVersionUID = -3269137608207801150L;
    private final int exitCode;

    ExitException(int exitCode) {
        this((String)null, exitCode);
    }

    ExitException(String message, int exitCode) {
        this(message, null, exitCode);
    }

    ExitException(Throwable cause, int exitCode) {
        this(null, cause, exitCode);
    }

    ExitException(String message, Throwable cause, int exitCode) {
        super(message, cause);
        if(exitCode == 0)
            throw new RuntimeException("Cannot create ExitException for exit code 0. "
                    + "The cause contained in this exception is the original exception (if any) "
                    + "handed over to constructor of the ExitException.", cause);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
