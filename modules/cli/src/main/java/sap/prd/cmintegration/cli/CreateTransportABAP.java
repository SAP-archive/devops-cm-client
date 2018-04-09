package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.http.CMODataAbapClient;

/**
 * Command for creating a transport for a change in SAP Solution Manager.
 */
@CommandDescriptor(name = "create-transport", type = BackendType.ABAP)
class CreateTransportABAP extends Command {

    private static class Opts {
        static Option owner = new Option("o", "owner", true, "The transport owner. If ommited the login user us used."),
                      description = new Option("d", "description", true, "The description of the transport request."),
                      targetSystem = new Option("ts", "target-system", true, "The target of the transport"),
                      transportType = new Option("tt", "transport-type", true, "The type of the transport, e.g. workbench, customizing."),
                      requestRef = new Option("rr", "request-ref", true, "The request reference.");
    }

    final static private Logger logger = LoggerFactory.getLogger(CreateTransportABAP.class);
    private final String owner, description, targetSystem, transportType, requestRef;

    public CreateTransportABAP(String host, String user, String password, String owner, String description, String targetSystem, String transportType, String requestRef) {
        super(host, user, password);
        this.owner = owner;
        this.description = description;
        this.targetSystem = targetSystem;
        this.transportType = transportType;
        this.requestRef = requestRef == null ? "" : requestRef;
    }

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        Command.addOpts(options);

        options.addOption(Opts.owner)
               .addOption(Opts.description)
               .addOption(Opts.targetSystem)
               .addOption(Opts.transportType)
               .addOption(Opts.requestRef);

        if(helpRequested(args)) {
            handleHelpOption(format("%s [--owner <owner>][--description <description>]", getCommandName(CreateTransportABAP.class)),
            "Creates a new transport entity. " +
            "Returns the ID of the transport entity. " +
            "If there is already an open transport, the ID of the already existing open transport might be returned.",
            new Options().addOption(Opts.owner).addOption(Opts.description).addOption(Opts.targetSystem)); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new CreateTransportABAP(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                commandLine.getOptionValue(Opts.owner.getOpt()),
                commandLine.getOptionValue(Opts.description.getOpt()),
                commandLine.getOptionValue(Opts.targetSystem.getOpt()),
                commandLine.getOptionValue(Opts.transportType.getOpt()),
                commandLine.getOptionValue(Opts.requestRef.getOpt())).execute();
    }

    @Override
    void execute() throws Exception {

        CMODataAbapClient client = AbapClientFactory.getInstance().newClient(host, user, password);

        logger.debug("Creating transport request.");

        String o = isBlank(owner) ? user : owner;

        if(isBlank(description)) {
            throw new CMCommandLineException("No description provided. Cannot create transports without description.");
        }

        if(isBlank(targetSystem)) {
            throw new CMCommandLineException("No target system provided. Cannot create transport without target system.");
        }

        if(isBlank(transportType)) {
            throw new CMCommandLineException("No transport type provided. Cannot create transport without transport type");
        }

        Transport transport = client.createTransport(Transport.getTransportCreationRequestMap(o, description, targetSystem, requestRef, transportType));

        logger.debug(format("Transport '%s' created. isModifiable: '%b', Owner: '%s', Description: '%s'.",
            transport.getTransportID(), transport.isModifiable(), transport.getOwner(), transport.getDescription()));

        System.out.println(transport.getTransportID());
        System.out.flush();
    }
}
