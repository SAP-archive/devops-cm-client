package sap.prd.cmintegration.cli;

import sap.prd.cmintegration.cli.TransportRetriever.BackendType;

/**
 * Root class for all commands.
 */
abstract class Command {

    protected final BackendType type;
    protected final String host, user, password;

    protected Command(BackendType type, String host, String user, String password) {
        this.type = type;
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
