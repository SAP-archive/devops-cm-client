package sap.prd.cmintegration.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class GetChangeStatus {

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("c", "change", true, "Retrieves a change.");
        options.addRequiredOption("u", "user", true, "User.");
        options.addRequiredOption("p", "password", true, "Password, if '-' if provided, password is read from stdin.");
        options.addRequiredOption("h", "host", true, "Host");

        CommandLine commandLine = new DefaultParser().parse(options, args);

        String changeId = commandLine.getOptionValue('c');
        String user = commandLine.getOptionValue('u');
        String password = commandLine.getOptionValue('p');
        if(password.equals("-")) password = readPassword();
        String host = commandLine.getOptionValue('h');

        System.out.println(new CMODataClient(host, user, password).getChange(changeId).getStatus());
    }

    private static String readPassword() throws IOException {
        return new BufferedReader(
                new InputStreamReader(System.in, "UTF-8")).readLine();
    }
}
