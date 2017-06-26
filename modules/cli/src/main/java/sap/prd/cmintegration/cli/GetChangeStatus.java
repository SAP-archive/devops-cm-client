package sap.prd.cmintegration.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;

public class GetChangeStatus {

    private String changeId;
    private String user;
    private String password;
    private String host;

    GetChangeStatus(String host, String user, String password, String changeId) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.changeId = changeId;
    }

    void execute() throws Exception {
        CMODataChange change = ClientFactory.getInstance().newClient(host, user, password).getChange(changeId);
        String status = change.getStatus();
        System.out.println(status);
    }

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("c", "change", true, "Retrieves a change.");
        options.addRequiredOption("u", "user", true, "User.");
        options.addRequiredOption("p", "password", true, "Password, if '-' if provided, password is read from stdin.");
        options.addRequiredOption("h", "host", true, "Host");

        CommandLine commandLine = new DefaultParser().parse(options, args);

        String host = commandLine.getOptionValue('h');
        String user = commandLine.getOptionValue('u');

        String password = commandLine.getOptionValue('p');
        if(password.equals("-")) password = readPassword();

        String changeId = commandLine.getOptionValue('c');

        new GetChangeStatus(host, user, password, changeId).execute();
    }

    private static String readPassword() throws IOException {
        return new BufferedReader(
                new InputStreamReader(System.in, "UTF-8")).readLine();
    }
}
