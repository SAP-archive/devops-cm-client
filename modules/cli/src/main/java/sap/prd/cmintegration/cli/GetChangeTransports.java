package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

@CommandDescriptor(name="get-transports")
class GetChangeTransports extends Command {

    final static private Logger logger = LoggerFactory.getLogger(GetChangeTransports.class);
    private final String changeId;

    private final boolean modifiableOnly;

    
    GetChangeTransports(String host, String user, String password, String changeId,
            boolean modifiableOnly) {
        super(host, user, password);
        this.changeId = changeId;
        this.modifiableOnly = modifiableOnly;
    }

    public final static void main(String[] args) throws Exception {

        logger.debug(format("%s called with arguments: '%s'.", GetChangeTransports.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);

        Option modifiableOnly = new Option("m", "modifiable-only", false, "Returns modifiable transports only.");
        options.addOption(modifiableOnly);

        if(helpRequested(args)) {
            handleHelpOption(format("%s [SUBCOMMAND_OPTIONS] <changeId>", getCommandName(GetChangeTransports.class)),
                    "Returns the ids of the transports for the change represented by <changeId>.",
                      new Options().addOption(modifiableOnly)); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new GetChangeTransports(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                commandLine.hasOption(modifiableOnly.getOpt())).execute();
    }

    @Override
    public void execute() throws Exception {
        Predicate<CMODataTransport> all = it -> true;
        Predicate<CMODataTransport> modOnly = it -> it.isModifiable();
        try (CMODataClient client = ClientFactory.getInstance().newClient(host, user, password)) {
            ArrayList<CMODataTransport> transports = client.getChangeTransports(changeId);
            transports.stream()
                .filter(modifiableOnly ? modOnly : all)
                .forEach(it ->{ System.out.println(it.getTransportID());
                                logger.debug(String.format("Tansport Id: '%s' Owner: '%s' isModifiable: '%s'", it.getTransportID(), it.getOwner(), Boolean.toString(it.isModifiable())));});
        }
    }
}
