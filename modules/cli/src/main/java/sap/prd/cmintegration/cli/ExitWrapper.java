package sap.prd.cmintegration.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExitWrapper {
    final static private Logger logger = LoggerFactory.getLogger(ExitWrapper.class);
    public final static void main(String[] args) throws Exception {
        try {
            Commands.main(args);
        } catch(ExitException e) {
            if(e.getCause() == null) {
                e.printStackTrace();
            } else {
                e.getCause().printStackTrace();
            }
            logger.error(e.getMessage(), e);
            System.exit(e.getExitCode());
        }
    }
}
