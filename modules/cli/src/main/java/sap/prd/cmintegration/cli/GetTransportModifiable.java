package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class GetTransportModifiable extends Command {

    private final String changeId, transportId;

    public GetTransportModifiable(String host, String user, String password, String changeId, String transportId) {
        super(host, user, password);
        this.changeId = changeId;
        this.transportId = transportId;
    }

    @Override
    void execute() throws Exception {
        Collection<CMODataTransport> changeTransports = ClientFactory.getInstance()
                .newClient(host,  user,  password)
                    .getChangeTransports(changeId);

        Optional<CMODataTransport> transport = changeTransports.stream().filter( it -> it.getTransportID().equals(transportId) ).findFirst();

        if(transport.isPresent()) {
            System.out.println(transport.get().isModifiable());
        }  else {
            throw new CMCommandLineException(String.format("Transport '%s' not found for change '%s'.", transportId, changeId));
        }
    }

    public final static void main(String[] args) throws Exception {
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);

        if(helpRequested(args)) {
            handleHelpOption("<changeId> <transportId>", options); return;
        }
        CommandLine commandLine = new DefaultParser().parse(options, args);

        new GetTransportModifiable(getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                getTransportId(commandLine)).execute();
    }

    private static String getTransportId(CommandLine commandLine) {
        try {
            return Commands.Helpers.getArg(commandLine, 1);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new CMCommandLineException("No transportId specified.");
        }
    }
}
