package sap.prd.cmintegration.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
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
        Command.Helpers.addStandardParameters(options);

        if(args.length >= 1 && args[0].equals("--help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("[options...] <changeId>", options);
            return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        String host = commandLine.getOptionValue('h');
        String user = commandLine.getOptionValue('u');

        String password = Command.Helpers.getPassword(commandLine);

        String[] _args = commandLine.getArgs();
        if(_args.length != 1) {
            throw new CMCommandLineException("No changeId specified.");
        }
        String changeId = _args[0];

        new GetChangeStatus(host, user, password, changeId).execute();
    }
}
