package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

@CommandDescriptor(name="release-transport")
class ReleaseTransport extends Command {

    final static private Logger logger = LoggerFactory.getLogger(ReleaseTransport.class);
    private final String changeId, transportId;

    ReleaseTransport(String host, String user, String password,
            String changeId, String transportId) {

        super(host, user, password);
        this.changeId = changeId;
        this.transportId = transportId;
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(Commands.Helpers.getArgsLogString(args));
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);

        if(helpRequested(args)) {
            handleHelpOption(
                format("%s <changeId> <transportId>", getCommandName(ReleaseTransport.class)),
                "Releases the transport specified by <changeId>, <transportId>.", new Options()); return;
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
        try (CMODataClient client = ClientFactory.getInstance().newClient(host,  user,  password)) {
            client.releaseDevelopmentTransport(changeId, transportId);
        }
    }

}
