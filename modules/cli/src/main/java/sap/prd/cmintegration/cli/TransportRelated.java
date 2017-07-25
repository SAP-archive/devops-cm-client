package sap.prd.cmintegration.cli;

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

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

abstract class TransportRelated extends Command {

    protected final String changeId, transportId;

    protected TransportRelated(String host, String user, String password,
            String changeId, String transportId) {
        super(host, user, password);
        this.changeId = changeId;
        this.transportId = transportId;
    }

    protected abstract Predicate<CMODataTransport> getOutputPredicate();

    @Override
    final void execute() throws Exception {

        try(CMODataClient client = ClientFactory.getInstance().newClient(host,  user,  password)) {

            Optional<CMODataTransport> transport = client.getChangeTransports(changeId).stream()
                .filter( it -> it.getTransportID().equals(transportId) ).findFirst();

            if(transport.isPresent()) {
                getOutputPredicate().test(transport.get());
            }  else {
                throw new CMCommandLineException(String.format("Transport '%s' not found for change '%s'.", transportId, changeId));
            }
        }
    }

    protected static void main(Class<? extends TransportRelated> clazz, String[] args, String usage, String helpText) throws Exception {

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
