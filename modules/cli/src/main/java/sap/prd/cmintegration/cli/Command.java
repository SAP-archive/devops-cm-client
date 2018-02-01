package sap.prd.cmintegration.cli;

/**
 * Root class for all commands.
 */
abstract class Command {

    protected final String host, user, password;

    protected Command(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    /**
     * Contains the command specific logic. E.g. performs a
     * call to SAP Solution Manager, parses the result and
     * provides it via System.out to the caller of the command line.
     * @throws Exception In case of trouble.
     */
    abstract void execute() throws Exception;
}
