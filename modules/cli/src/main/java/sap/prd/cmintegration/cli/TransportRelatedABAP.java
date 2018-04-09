package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;

import com.sap.cmclient.Transport;
import com.sap.cmclient.http.UnexpectedHttpResponseException;

abstract class TransportRelatedABAP extends TransportRelated {

    protected TransportRelatedABAP(String host, String user, String password,
            String transportId) {
        super(host, user, password, transportId);
    }

    protected static void main(Class<? extends TransportRelated> clazz, Options options, String[] args, String usage, String helpText) throws Exception {

        logger.debug(format("%s called with arguments: %s", clazz.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

        Command.addOpts(options);
        options.addOption(Opts.TRANSPORT_ID);

        if(helpRequested(args)) {
            handleHelpOption(usage, helpText, new Options().addOption(Opts.TRANSPORT_ID)); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        newInstance(clazz,
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getTransportId(commandLine)).execute();
    }

    private static TransportRelated newInstance(Class<? extends TransportRelated> clazz, String host, String user, String password, String transportId) {
        try {
            return clazz.getDeclaredConstructor(new Class[] {String.class, String.class, String.class, String.class})
            .newInstance(new Object[] {host, user, password, transportId});
        } catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(format("Cannot instanciate class '%s'.", clazz.getName()),e);
        }
    }

    protected Optional<Transport> getTransport() {
        com.sap.cmclient.dto.Transport transport;
        try {
            transport = AbapClientFactory.getInstance().newClient(host, user, password).getTransport(transportId);
            return transport != null ? Optional.of(transport) : Optional.empty();
        } catch (EntityProviderException | EdmException | UnexpectedHttpResponseException | IOException
                | URISyntaxException e) {
            throw new RuntimeException(String.format("Cannot retrieve transport for transportId '%s'.", transportId), e);
        }
    }
}
