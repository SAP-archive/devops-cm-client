package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getBackendType;
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

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;
import sap.prd.cmintegration.cli.TransportRetriever.BackendType;

/**
 * Command for retrieving the status of a change.
 */
@CommandDescriptor(name = "is-change-in-development")
class GetChangeStatus extends Command {

    final static private Logger logger = LoggerFactory.getLogger(GetChangeStatus.class);
    private String changeId;

    GetChangeStatus(BackendType backendType, String host, String user, String password, String changeId) {
        super(backendType, host, user, password);
        this.changeId = changeId;
    }

    @Override
    void execute() throws Exception {
        try (CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(host, user, password)) {
            CMODataChange change = client.getChange(changeId);
            logger.debug(format("Change '%s' retrieved from host '%s'. isInDevelopment: '%b'.", change.getChangeID(), host, change.isInDevelopment()));
            System.out.println(change.isInDevelopment());
        } catch(Exception e) {
            logger.warn(format("Change '%s' could not be retrieved from '%s'.", changeId, host), e);
            throw e;
        }
    }

    public final static void main(String[] args) throws Exception {

        logger.debug(format("%s called with arguments: '%s'.", GetChangeStatus.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);
        options.addOption(Commands.CMOptions.CHANGE_ID);

        if(helpRequested(args)) {
            handleHelpOption(format("%s -cID <changeId>", getCommandName(GetChangeStatus.class)),
                    "Returns 'true' if the change specified by <changeId> is in development. Otherwise 'false'. This command is only available for SOLMAN backends.", new Options()); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        BackendType backendType = getBackendType(commandLine);

        new GetChangeStatus(
                backendType,
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(backendType, commandLine)).execute();
    }
}
