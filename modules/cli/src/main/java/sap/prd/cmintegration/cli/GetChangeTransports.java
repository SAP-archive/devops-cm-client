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

/**
 * Command for for retrieving the transport of a change. Depending on the options
 * handed over to that command only the mofifiable transports are
 * returned.
 */
@CommandDescriptor(name="get-transports")
class GetChangeTransports extends Command {

    private final static Option modifiableOnlyOption = new Option("m", "modifiable-only", false, "Returns modifiable transports only.");

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


        options.addOption(modifiableOnlyOption);

        if(helpRequested(args)) {
            handleHelpOption(format("%s [SUBCOMMAND_OPTIONS] <changeId>", getCommandName(GetChangeTransports.class)),
                    "Returns the ids of the transports for the change represented by <changeId>.",
                      new Options().addOption(modifiableOnlyOption)); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new GetChangeTransports(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                commandLine.hasOption(modifiableOnlyOption.getOpt())).execute();
    }

    @Override
    public void execute() throws Exception {

        if(modifiableOnly) {
            logger.debug(format("Flag '-%s' has been set. Only modifiable transports will be returned.", modifiableOnlyOption.getOpt()));
        } else {
            logger.debug(format("Flag '-%s' has not beem set. All transports will be returned.", modifiableOnlyOption.getOpt()));
        }

        Predicate<CMODataTransport> log =
                it -> {
                    logger.debug(format("Transport '%s' retrieved from host '%s'. isModifiable: '%b', Owner: '%s', Description: '%s'.",
                      it.getTransportID(),
                      host,
                      it.isModifiable(),
                      it.getOwner(),
                      it.getDescription()));
                    return true;};

        Predicate<CMODataTransport> all = it -> true;

        Predicate<CMODataTransport> modOnly = it -> {
              if(!it.isModifiable()) {
                logger.debug(format("Transport '%s' is modifiable. This transport is added to the result set.", it.getTransportID()));
              }
              else {
                logger.debug(format("Transport '%s' is not modifiable. This transport is not added to the result set.", it.getTransportID()));
              };
              return it.isModifiable();};

        try (CMODataClient client = ClientFactory.getInstance().newClient(host, user, password)) {
            ArrayList<CMODataTransport> transports = client.getChangeTransports(changeId);

            if(transports.isEmpty())  {
                logger.debug(format("No transports retrieved for change document id '%s' from host '%s'.", changeId, host));
            }

            transports.stream().filter(log)
                .filter(modifiableOnly ? modOnly : all)
                .forEach(it ->System.out.println(it.getTransportID()));
        } catch(Exception e) {
            logger.error(format("Exception caught while retrieving transports for change document '%s' from host '%s',", changeId, host), e);
            throw e;
        }
    }
}
