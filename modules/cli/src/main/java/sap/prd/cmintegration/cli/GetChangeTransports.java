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

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.sap.cmclient.Transport;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

/**
 * Command for for retrieving the transport of a change. Depending on the options
 * handed over to that command only the mofifiable transports are
 * returned.
 */
@CommandDescriptor(name="get-transports", type = BackendType.SOLMAN)
class GetChangeTransports extends Command {

    private static class Opts {

        final static Option modifiableOnlyOption = new Option("m", "modifiable-only", false, "Returns modifiable transports only.");

        static Options addOptions(Options options, boolean includeStandardOptions) {
            if(includeStandardOptions) {
                Command.addOpts(options);
            }
            options.addOption(Commands.CMOptions.CHANGE_ID);
            options.addOption(modifiableOnlyOption);
            return options;
        }
    }

    final static private Logger logger = LoggerFactory.getLogger(GetChangeTransports.class);
    private final String changeId;

    private final boolean modifiableOnly;

    GetChangeTransports(String host, String user, String password, String changeId,
            boolean modifiableOnly) {

        super(host, user, password);

        Preconditions.checkArgument(! isBlank(changeId), "No changeId provided.");

        this.changeId = changeId;
        this.modifiableOnly = modifiableOnly;
    }

    public final static void main(String[] args) throws Exception {

        if(helpRequested(args)) {
            handleHelpOption(getCommandName(GetChangeTransports.class), "",
                    "Returns the ids of the transports for the given change.",
                    Opts.addOptions(Opts.addOptions(new Options(), false), false));
            return;
        }

        CommandLine commandLine = new DefaultParser().parse(Opts.addOptions(new Options(), true), args);

        new GetChangeTransports(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                commandLine.hasOption(Opts.modifiableOnlyOption.getOpt())).execute();
    }

    @Override
    public void execute() throws Exception {

        if(modifiableOnly) {
            logger.debug(format("Flag '-%s' has been set. Only modifiable transports will be returned.", Opts.modifiableOnlyOption.getOpt()));
        } else {
            logger.debug(format("Flag '-%s' has not beem set. All transports will be returned.", Opts.modifiableOnlyOption.getOpt()));
        }

        Predicate<Transport> log =
                it -> {
                    logger.debug(format("Transport '%s' retrieved from host '%s'. isModifiable: '%b', Owner: '%s', Description: '%s'.",
                      it.getTransportID(),
                      host,
                      it.isModifiable(),
                      it.getOwner(),
                      it.getDescription()));
                    return true;};

        Predicate<Transport> all = it -> true;

        Predicate<Transport> modOnly = it -> {
              if(!it.isModifiable()) {
                logger.debug(format("Transport '%s' is modifiable. This transport is added to the result set.", it.getTransportID()));
              }
              else {
                logger.debug(format("Transport '%s' is not modifiable. This transport is not added to the result set.", it.getTransportID()));
              };
              return it.isModifiable();};

        try (CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(host, user, password)) {
            List<Transport> transports = client.getChangeTransports(changeId);

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
