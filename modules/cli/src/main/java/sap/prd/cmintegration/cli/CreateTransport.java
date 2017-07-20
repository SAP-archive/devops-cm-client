package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;

public class CreateTransport extends Command {

    private final String changeId;

    public CreateTransport(String host, String user, String password, String changeId) {
        super(host, user, password);
        this.changeId = changeId;
    }

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);

        if(handleHelpOption(args, "<changeId>", options)) return;

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new CreateTransport(getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine)).execute();
    }

    @Override
    void execute() throws Exception {
        CMODataTransport transport = ClientFactory.getInstance().newClient(host, user,  password)
            .createDevelopmentTransport(changeId);
        System.out.println(transport.getTransportID());
        System.out.flush();
    }
}
