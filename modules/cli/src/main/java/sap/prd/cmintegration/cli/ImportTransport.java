package sap.prd.cmintegration.cli;

import static java.lang.String.format;
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
import org.apache.commons.cli.ParseException;

import com.sap.cmclient.http.UnexpectedHttpResponseException;

import sap.prd.cmintegration.cli.TransportRelated.Opts;

@CommandDescriptor(name="import-transport", type = BackendType.ABAP)
public class ImportTransport extends Command {

    private final static Option targetSystem = new Option("ts", "target-system", true, "The target system");

    static {
        targetSystem.setArgName("targetSystem");
    }

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

    public final static void main(String[] args) throws Exception {
        Options options = new Options();
        Command.addOpts(options);
        options.addOption(Opts.TRANSPORT_ID);
        options.addOption(targetSystem);

        if(helpRequested(args)) {
            handleHelpOption(format("%s -%s <%s> -%s <%s>", ImportTransport.class.getAnnotation(CommandDescriptor.class).name(),
                                                            targetSystem.getOpt(),
                                                            targetSystem.getArgName(),
                                                            Opts.TRANSPORT_ID.getOpt(),
                                                            Opts.TRANSPORT_ID.getArgName()),
                    "Imports a transport into a system", new Options()); return;
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
        return commandLine.getOptionValue(targetSystem.getOpt());
    }
}
