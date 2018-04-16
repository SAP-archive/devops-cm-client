package sap.prd.cmintegration.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import com.sap.cmclient.Transport;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

abstract class TransportRelatedSOLMAN extends TransportRelated {

    protected final String changeId;

    static class Opts {
        static Options addOptions(Options opts, boolean includeStandardOpts) {
            TransportRelated.Opts.addOpts(opts, includeStandardOpts);
            opts.addOption(Commands.CMOptions.CHANGE_ID);
            return opts;
        }
    }

    protected TransportRelatedSOLMAN(String host, String user, String password,
            String changeId, String transportId, boolean returnCodeMode) {

        super(host, user, password, transportId, returnCodeMode);

        checkArgument(! isBlank(changeId), "No changeId provided.");
        this.changeId = changeId;
    }

    protected static void main(Class<? extends TransportRelatedSOLMAN> clazz, Options options, String[] args, String subCommandName, String argumentDocu, String helpText) throws Exception {

        logger.debug(format("%s called with arguments: %s", clazz.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

        if(helpRequested(args)) {
            handleHelpOption(subCommandName, argumentDocu, helpText, TransportRelatedSOLMAN.Opts.addOptions(new Options(), false));
            return;
        }

        TransportRelatedSOLMAN.Opts.addOptions(options, true);
        CommandLine commandLine = new DefaultParser().parse(options, args);

        newInstance(clazz,
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                getTransportId(commandLine),
                isReturnCodeMode(commandLine)).execute();
    }

    protected void execute() throws Exception {
        try {
            super.execute();
        } catch(TransportNotFoundException e) {
            throw new CMCommandLineException(format("Transport '%s' not found for change '%s'.", e.getTransportId(), changeId), e);
        }
    }

    private static TransportRelated newInstance(Class<? extends TransportRelatedSOLMAN> clazz, String host, String user, String password, String changeId, String transportId, boolean returnCodeMode) {
        try {
            return clazz.getDeclaredConstructor(new Class[] {String.class, String.class, String.class, String.class, String.class, Boolean.TYPE})
            .newInstance(new Object[] {host, user, password, changeId, transportId, returnCodeMode});
        } catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(format("Cannot instanciate class '%s'.", clazz.getName()),e);
        }
    }


    protected Optional<Transport> getTransport() {
        try(CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(host, user, password)) {
            return client.getChangeTransports(changeId)
                       .stream()
                       .filter( it -> it.getTransportID().equals(transportId) )
                       .findFirst();
        }
    }
}
