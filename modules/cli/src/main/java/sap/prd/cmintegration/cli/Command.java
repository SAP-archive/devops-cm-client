package sap.prd.cmintegration.cli;

abstract class Command {

    protected final String host, user, password;

    protected Command(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    abstract void execute() throws Exception;
}
