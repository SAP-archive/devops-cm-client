package sap.prd.cmintegration.cli;

import static java.lang.String.format;
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
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

/**
 * Base class for all transport related commands.
 */
abstract class TransportRelated extends Command {

    final static private Logger logger = LoggerFactory.getLogger(TransportRelated.class);
    protected final String changeId, transportId;

    protected TransportRelated(String host, String user, String password,
            String changeId, String transportId) {
        super(host, user, password);
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

        if(helpRequested(args)) {
            handleHelpOption(usage, helpText, new Options()); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        newInstance(clazz, getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                getTransportId(commandLine)).execute();
    }

    private Optional<Transport> getTransport(String changeId, String transportId) {
        try(CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(host, user, password)) {
            return client.getChangeTransports(changeId).stream()
                .filter( it -> it.getTransportID().equals(transportId) ).findFirst();
        } catch(RuntimeException e) {
            logger.warn(format("Exception caught while getting transport '%s' for change document '%s' from host '%s'.", transportId, changeId, host), e);
            throw e;
        }
    }

    private static TransportRelated newInstance(Class<? extends TransportRelated> clazz, String host, String user, String password, String changeId, String transportId) {
        try {
            return clazz.getDeclaredConstructor(new Class[] {String.class, String.class, String.class, String.class, String.class})
            .newInstance(new Object[] {host, user, password, changeId, transportId});
        } catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static String getTransportId(CommandLine commandLine) {
        try {
            return Commands.Helpers.getArg(commandLine, 2);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new CMCommandLineException("No transportId specified.");
        }
    }

}
