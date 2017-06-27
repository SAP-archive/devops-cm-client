package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Command.Helpers.getHost;
import static sap.prd.cmintegration.cli.Command.Helpers.getUser;
import static sap.prd.cmintegration.cli.Command.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Command.Helpers.getChangeId;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class GetChangeTransports {

    private final String host, user, password, changeId;
    
    GetChangeTransports(String host, String user, String password, String changeId) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.changeId = changeId;
    }

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        Command.Helpers.addStandardParameters(options);

        if(args.length >= 1 && args[0].equals("--help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(" ", options);
            return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new GetChangeTransports(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine)).execute();
    }

    public void execute() throws Exception {
        ArrayList<CMODataTransport> transports = ClientFactory.getInstance().newClient(host, user, password).getChangeTransports(changeId);
        for(CMODataTransport transport : transports) {
            System.out.println(transport.getTransportID());
        }
    }
}
