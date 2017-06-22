package sap.prd.cmintegration.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class GetChangeStatus {

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("c", "change", true, "Retrieves a change.");
        options.addRequiredOption("u", "user", true, "User.");
        options.addRequiredOption("p", "password", true, "Password");
        // REVISIT we should not deal with a password like in the line above. Maybe read via stdin?
        options.addRequiredOption("h", "host", true, "Host");

        CommandLine commandLine = new DefaultParser().parse(options, args);

        String changeId = commandLine.getOptionValue('c');
        String user = commandLine.getOptionValue('u');
        String password = commandLine.getOptionValue('p');
        String host = commandLine.getOptionValue('h');

        System.out.println(new CMODataClient(host, user, password).getChange(changeId).getStatus());
    }

}
