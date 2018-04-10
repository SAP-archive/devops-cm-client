package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
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

import com.google.common.base.Preconditions;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

/**
 * Command for retrieving the status of a change.
 */
@CommandDescriptor(name = "is-change-in-development", type = BackendType.SOLMAN)
class GetChangeStatus extends Command {

    static class Opts {

        static Options addOptions(Options opts, boolean includeStandardOptions) {

            if(includeStandardOptions) {
                Command.addOpts(opts);
            }

            return opts.addOption(Commands.CMOptions.CHANGE_ID);
        }
    }

    final static private Logger logger = LoggerFactory.getLogger(GetChangeStatus.class);
    private String changeId;

    GetChangeStatus(String host, String user, String password, String changeId) {

        super(host, user, password);

        Preconditions.checkArgument(! isBlank(changeId), "No changeId provided.");

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

        if(helpRequested(args)) {
            handleHelpOption(getCommandName(GetChangeStatus.class), "",
                    "Returns 'true' if the given change is in development. Otherwise 'false'.", Opts.addOptions(new Options(), false));
            return;
        }

        CommandLine commandLine = new DefaultParser().parse(Opts.addOptions(new Options(), true), args);

        new GetChangeStatus(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine)).execute();
    }
}
