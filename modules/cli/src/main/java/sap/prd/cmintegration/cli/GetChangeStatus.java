package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;

public class GetChangeStatus extends Command {

    private String changeId;

    GetChangeStatus(String host, String user, String password, String changeId) {
        super(host, user, password);
        this.changeId = changeId;
    }

    @Override
    void execute() throws Exception {
        CMODataChange change = ClientFactory.getInstance().newClient(host, user, password).getChange(changeId);
        System.out.println(change.isInDevelopment());
    }

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);

        if(handleHelpOption(args, "<changeId>", options)) return;

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new GetChangeStatus(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine)).execute();
    }
}
