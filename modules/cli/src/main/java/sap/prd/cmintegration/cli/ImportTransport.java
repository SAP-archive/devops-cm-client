package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.CMOptions.newOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;

import com.sap.cmclient.http.UnexpectedHttpResponseException;

@CommandDescriptor(name="import-transport", type = BackendType.CTS)
public class ImportTransport extends Command {

    static class Opts {

        final static Option targetSystem = newOption("ts", "target-system", "The target system", "targetSystem", true);

        static Options addOptions(Options opts, boolean includeStandardOptions) {

            if(includeStandardOptions) {
                Command.addOpts(opts);
            }

            return opts.addOption(targetSystem)
                       .addOption(TransportRelated.Opts.TRANSPORT_ID);
        }
    }

    private final String systemId, transportId;

    public ImportTransport(String host, String user, String password, String systemId, String transportId) {
        super(host, user, password);
        this.systemId = systemId;
        this.transportId = transportId;
    }

    @Override
    void execute() throws UnexpectedHttpResponseException, IOException, URISyntaxException, EntityProviderException, EdmException {
        AbapClientFactory.getInstance().newClient(host, user, password).importTransport(systemId, transportId);
    }

    public final static void main(String[] args) throws Exception {

        if(helpRequested(args)) {
            handleHelpOption(getCommandName(ImportTransport.class), "",
                    "Imports a transport into a system", Opts.addOptions(new Options(), false)); return;
        }

        CommandLine commandLine = new DefaultParser().parse(Opts.addOptions(new Options(), true), args);

        new ImportTransport(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getSystemId(commandLine),
                TransportRelated.getTransportId(commandLine)).execute();
    }

    private static String getSystemId(CommandLine commandLine) {
        return commandLine.getOptionValue(Opts.targetSystem.getOpt());
    }
}
