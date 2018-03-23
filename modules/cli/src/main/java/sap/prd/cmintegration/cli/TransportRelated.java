package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getBackendType;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;

import sap.prd.cmintegration.cli.TransportRetriever.BackendType;

/**
 * Base class for all transport related commands.
 */
abstract class TransportRelated extends Command {

    protected static class Opts {
        protected final static Option TRANSPORT_ID = new Option("tID", "transport-id", true, "transportID");
    }

    final static private Logger logger = LoggerFactory.getLogger(TransportRelated.class);
    protected final String changeId, transportId;

    protected TransportRelated(BackendType type, String host, String user, String password,
            String changeId, String transportId) {
        super(type, host, user, password);
        this.changeId = changeId;
        this.transportId = transportId;
    }

    protected abstract Predicate<Transport> getOutputPredicate();

    @Override
    final void execute() throws Exception {

        Optional<Transport> transport = getTransport(changeId, transportId);
        if(transport.isPresent()) {
            Transport t = transport.get();
 
            if(!t.getTransportID().trim().equals(transportId.trim())) {
                throw new CMCommandLineException(
                    format("TransportId of resolved transport ('%s') does not match requested transport id ('%s').",
                            t.getTransportID(),
                            transportId));
            }
 
            logger.debug(format("Transport '%s' has been found for change document '%s'. isModifiable: '%b', Owner: '%s', Description: '%s'.",
                    transportId, changeId,
                    t.isModifiable(), t.getOwner(), t.getDescription()));
 
            getOutputPredicate().test(t);
        }  else {
            throw new CMCommandLineException(String.format("Transport '%s' not found for change '%s'.", transportId, changeId));
        }
    }

    protected static void main(Class<? extends TransportRelated> clazz, String[] args, String usage, String helpText) throws Exception {

        logger.debug(format("%s called with arguments: %s", clazz.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);
        options.addOption(Commands.CMOptions.CHANGE_ID);
        options.addOption(Opts.TRANSPORT_ID);

        if(helpRequested(args)) {
            handleHelpOption(usage, helpText, new Options()); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        BackendType backendType = getBackendType(commandLine);

        newInstance(clazz,
                backendType,
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(backendType, commandLine),
                getTransportId(commandLine)).execute();
    }

    private Optional<Transport> getTransport(String changeId, String transportId) {
            return TransportRetriever.get(type, host, user, password).getTransport(type, changeId, transportId);
    }

    private static TransportRelated newInstance(Class<? extends TransportRelated> clazz, BackendType backendType, String host, String user, String password, String changeId, String transportId) {
        try {
            return clazz.getDeclaredConstructor(new Class[] {BackendType.class, String.class, String.class, String.class, String.class, String.class})
            .newInstance(new Object[] {backendType, host, user, password, changeId, transportId});
        } catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static String getTransportId(CommandLine commandLine) {
        String transportID = commandLine.getOptionValue(Opts.TRANSPORT_ID.getOpt());
        if(StringUtils.isEmpty(transportID)) {
            throw new CMCommandLineException("No transportId specified.");
        }
        return transportID;
    }

}
