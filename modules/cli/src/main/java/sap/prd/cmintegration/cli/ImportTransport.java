package sap.prd.cmintegration.cli;

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
import org.apache.commons.cli.ParseException;

import com.sap.cmclient.http.UnexpectedHttpResponseException;

import sap.prd.cmintegration.cli.TransportRelated.Opts;

@CommandDescriptor(name="import-transport", type = BackendType.ABAP)
public class ImportTransport extends Command {

    private final String systemId, transportId;

    public ImportTransport(String host, String user, String password, String systemId, String transportId) {
        super(host, user, password);
        this.systemId = systemId;
        this.transportId = transportId;
    }

    @Override
    void execute() throws UnexpectedHttpResponseException, IOException, URISyntaxException {
        AbapClientFactory.getInstance().newClient(host, user, password).importTransport(systemId, transportId);
    }

    public final static void main(String[] args) throws IOException, ParseException, UnexpectedHttpResponseException, URISyntaxException {
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);
        options.addOption(Commands.CMOptions.CHANGE_ID);
        options.addOption(Opts.TRANSPORT_ID);

        if(helpRequested(args)) {
            handleHelpOption("TODO", "TODO", new Options()); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new ImportTransport(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getSystemId(commandLine),
                TransportRelated.getTransportId(commandLine)).execute();
    }

    private static String getSystemId(CommandLine commandLine) {
        return "A5T"; // TODO: implement
    }
}
