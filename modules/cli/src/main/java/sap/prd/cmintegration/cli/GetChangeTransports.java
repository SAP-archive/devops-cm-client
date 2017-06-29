package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class GetChangeTransports extends Command {

    private final String changeId;
    
    GetChangeTransports(String host, String user, String password, String changeId) {
        super(host, user, password);
        this.changeId = changeId;
    }

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);

        if(handleHelpOption(args, "<changeId>", options)) return;

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new GetChangeTransports(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine)).execute();
    }

    @Override
    public void execute() throws Exception {
        ArrayList<CMODataTransport> transports = ClientFactory.getInstance().newClient(host, user, password).getChangeTransports(changeId);
        transports.stream().forEach(it -> System.out.println(it.getTransportID()));
    }
}
