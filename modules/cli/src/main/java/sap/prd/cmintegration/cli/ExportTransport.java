package sap.prd.cmintegration.cli;

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
import org.apache.commons.cli.Options;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;

import com.sap.cmclient.http.UnexpectedHttpResponseException;

@CommandDescriptor(name="export-transport", type = BackendType.ABAP)
public class ExportTransport extends Command {

    static class Opts {

        static Options addOptions(Options opts, boolean addStandardOpts) {
            if(addStandardOpts) {
                Command.addOpts(opts);
            }

            return opts.addOption(Commands.CMOptions.CHANGE_ID)
                       .addOption(sap.prd.cmintegration.cli.TransportRelated.Opts.TRANSPORT_ID);
        }
    }
    private final String transportId;

    public ExportTransport(String host, String user, String password, String transportId) {
        super(host, user, password);
        this.transportId = transportId;
    }

    @Override
    void execute() throws UnexpectedHttpResponseException, IOException, URISyntaxException, EntityProviderException, EdmException {
        AbapClientFactory.getInstance().newClient(host, user, password).releaseTransport(transportId);
    }

    public final static void main(String[] args) throws Exception {

        if(helpRequested(args)) {
            handleHelpOption(getCommandName(ExportTransport.class), "",
                             "Exports a transport.", Opts.addOptions(new Options(), false));
            return;
        }

        CommandLine commandLine = new DefaultParser().parse(Opts.addOptions(new Options(), true), args);

        new ExportTransport(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                TransportRelated.getTransportId(commandLine)).execute();
    }
}
