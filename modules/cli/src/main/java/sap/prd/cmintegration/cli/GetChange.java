package sap.prd.cmintegration.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class GetChange {

    public final static void main(String[] args) throws Exception {

        Option optChange = new Option("c", "change", true, "Retrieves a change.");
        Option optUser = new Option("u", "user", true, "User.");
        Option optPassword = new Option("p", "password", true, "Password");
        // REVISIT we should not deal with a password like in the line above. Maybe read via stdin?
        Option optHost = new Option("h", "host", true, "Host");

        optChange.setRequired(true);
        optUser.setRequired(true);
        optPassword.setRequired(true);
        optHost.setRequired(true);

        Options options = new Options();
        options.addOption(optChange);
        options.addOption(optUser);
        options.addOption(optPassword);
        options.addOption(optHost);

        CommandLine commandLine = new DefaultParser().parse(options, args);

        String changeId = commandLine.getOptionValue('c');
        String user = commandLine.getOptionValue('u');
        String password = commandLine.getOptionValue('p');
        String host = commandLine.getOptionValue('h');

        System.err.println("ChangeId: " + changeId);
        System.err.println("User: " + user);
        System.err.println("Host: " + host);

        new CMODataClient(host, user, password).getChange(changeId);
    }

}
