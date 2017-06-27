package sap.prd.cmintegration.cli;

public abstract class Command {

    protected final String host, user, password;

    public Command(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    abstract void execute() throws Exception;
}
