package sap.prd.cmintegration.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExitWrapper {
    final static private Logger logger = LoggerFactory.getLogger(ExitWrapper.class);
    public final static void main(String[] args) throws Exception {
        try {
            Commands.main(args);
        } catch(ExitException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace(System.err);
            System.exit(e.getExitCode());
        }
    }
}
