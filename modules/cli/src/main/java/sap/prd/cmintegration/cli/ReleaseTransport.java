package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

class ReleaseTransport extends Command {

    private final String changeId, transportId;

    ReleaseTransport(String host, String user, String password,
            String changeId, String transportId) {

        super(host, user, password);
        this.changeId = changeId;
        this.transportId = transportId;
    }

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);

        if(helpRequested(args)) {
            handleHelpOption("<changeId> <transportId>", options); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new ReleaseTransport(getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                TransportRelated.getTransportId(commandLine)).execute();
    }

    @Override
    void execute() throws Exception {
        ClientFactory.getInstance().newClient(host,  user,  password)
            .releaseDevelopmentTransport(changeId, transportId);
    }

}
