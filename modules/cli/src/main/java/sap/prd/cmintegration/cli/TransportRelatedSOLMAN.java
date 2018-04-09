package sap.prd.cmintegration.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.sap.cmclient.Transport;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

abstract class TransportRelatedSOLMAN extends TransportRelated {

    protected final String changeId;

    protected TransportRelatedSOLMAN(String host, String user, String password,
            String changeId, String transportId) {

        super(host, user, password, transportId);

        checkArgument(! isBlank(changeId), "No changeId provided.");
        this.changeId = changeId;
    }

    protected static void main(Class<? extends TransportRelated> clazz, Options options, String[] args, String usage, String helpText) throws Exception {

        logger.debug(format("%s called with arguments: %s", clazz.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

        addOpts(options);
        TransportRelated.Opts.addOpts(options);
        options.addOption(Commands.CMOptions.CHANGE_ID);

        if(helpRequested(args)) {
            handleHelpOption(usage, helpText, new Options()); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        newInstance(clazz,
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                getTransportId(commandLine)).execute();
    }

    protected void execute() throws Exception {
        try {
            super.execute();
        } catch(TransportNotFoundException e) {
            throw new CMCommandLineException(format("Transport '%s' not found for change '%s'.", e.getTransportId(), changeId), e);
        }
    }

    private static TransportRelated newInstance(Class<? extends TransportRelated> clazz, String host, String user, String password, String changeId, String transportId) {
        try {
            return clazz.getDeclaredConstructor(new Class[] {String.class, String.class, String.class, String.class, String.class})
            .newInstance(new Object[] {host, user, password, changeId, transportId});
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
