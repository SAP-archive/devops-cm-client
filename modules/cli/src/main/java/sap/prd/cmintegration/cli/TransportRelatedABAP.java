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
            String transportId, boolean returnCodeMode) {
        super(host, user, password, transportId, returnCodeMode);
    }

    protected static void main(Class<? extends TransportRelatedABAP> clazz, Options options, String[] args, String usage, String argumentDocu, String helpText) throws Exception {

        logger.debug(format("%s called with arguments: %s", clazz.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

        if(helpRequested(args)) {
            handleHelpOption(usage, argumentDocu, helpText, TransportRelated.Opts.addOpts(options, false));
            return;
        }

        TransportRelated.Opts.addOpts(options, true);

        CommandLine commandLine = new DefaultParser().parse(options, args);

        newInstance(clazz,
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getTransportId(commandLine),
                isReturnCodeMode(commandLine)).execute();
    }

    private static TransportRelated newInstance(Class<? extends TransportRelatedABAP> clazz, String host, String user, String password, String transportId, boolean returnCodeMode) {
        try {
            return clazz.getDeclaredConstructor(new Class[] {String.class, String.class, String.class, String.class, Boolean.TYPE})
            .newInstance(new Object[] {host, user, password, transportId, returnCodeMode});
        } catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(format("Cannot instanciate class '%s'.", clazz.getName()),e);
        }
    }

    protected Optional<Transport> getTransport() throws UnexpectedHttpResponseException {
        com.sap.cmclient.dto.Transport transport;
        try {
            transport = AbapClientFactory.getInstance().newClient(host, user, password).getTransport(transportId);
            return transport != null ? Optional.of(transport) : Optional.empty();
        } catch (EntityProviderException | EdmException | IOException
                | URISyntaxException e) {
            throw new RuntimeException(String.format("Cannot retrieve transport for transportId '%s'.", transportId), e);
        }
    }
}
