package sap.prd.cmintegration.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;

public class GetChangeStatus {

    private static String changeId;
    private static String user;
    private static String password;
    private static String host;

    static String getChangeId() {
        return changeId;
    }

    static String getUser() {
        return user;
    }

    static String getPassword() {
        return password;
    }

    static String getHost() {
        return host;
    }

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("c", "change", true, "Retrieves a change.");
        options.addRequiredOption("u", "user", true, "User.");
        options.addRequiredOption("p", "password", true, "Password, if '-' if provided, password is read from stdin.");
        options.addRequiredOption("h", "host", true, "Host");

        CommandLine commandLine = new DefaultParser().parse(options, args);

        changeId = commandLine.getOptionValue('c');
        user = commandLine.getOptionValue('u');
        password = commandLine.getOptionValue('p');
        if(password.equals("-")) password = readPassword();
        host = commandLine.getOptionValue('h');

        CMODataChange change = ClientFactory.getInstance().newClient(host, user, password).getChange(changeId);
        String status = change.getStatus();
        System.out.println(status);
    }

    private static String readPassword() throws IOException {
        return new BufferedReader(
                new InputStreamReader(System.in, "UTF-8")).readLine();
    }
}
