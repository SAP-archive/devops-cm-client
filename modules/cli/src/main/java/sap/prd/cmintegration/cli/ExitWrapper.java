package sap.prd.cmintegration.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher class which launches another class and handels {@link ExitException}s. In case
 * an ExitExcpetion is encountered the exit code contained in that exception is used
 * as return code when exiting the Java Virtual Machine.
 */
class ExitWrapper {
    final static private Logger logger = LoggerFactory.getLogger(ExitWrapper.class);
    public final static void main(String[] args) throws Exception {
        try {
            Commands.main(args);
        } catch(ExitException e) {
            if(e.getExitCode() != ExitException.ExitCodes.FALSE) {
                if(e.getCause() == null) {
                    e.printStackTrace();
                } else {
                    e.getCause().printStackTrace();
                }
                logger.error(e.getMessage(), e);
            }
            System.exit(e.getExitCode());
        }
    }
}
