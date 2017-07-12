package sap.prd.cmintegration.cli;

public class ExitWrapper {

    public final static void main(String[] args) throws Exception {
        try {
            Commands.main(args);
        } catch(ExitException e) {
            e.printStackTrace(System.err);
            System.exit(e.getExitCode());
        }
    }
}
