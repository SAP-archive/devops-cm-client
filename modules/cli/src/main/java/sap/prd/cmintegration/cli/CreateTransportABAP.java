package sap.prd.cmintegration.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;
import static sap.prd.cmintegration.cli.Commands.CMOptions.newOption;

import java.util.HashSet;
import java.util.Set;

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
        private static Set<Option> options = new HashSet<>();

        static Option owner = register(newOption("o", "owner", "The transport owner. If ommited the login user us used.", "owner", true)),
                      description = register(newOption("d", "description", "The description of the transport request.", "desc", true)),
                      targetSystem = register(newOption("ts", "target-system", "The target of the transport", "target", true)),
                      transportType = register(newOption("tt", "transport-type", "The type of the transport, e.g. workbench, customizing.", "type", true)),
                      requestRef = register(newOption("rr", "request-ref", "The request reference.", "ref", true));

        private static Option register(Option o) {
            options.add(o);
            return o;
        }
        static Options addOpts(Options opts) {
            options.stream().forEach( o -> opts.addOption(o));
            return opts;
        }
    }

    final static private Logger logger = LoggerFactory.getLogger(CreateTransportABAP.class);

    private final String owner,
                         description,
                         targetSystem,
                         transportType,
                         requestRef;

    public CreateTransportABAP(String host,
                               String user,
                               String password,
                               String owner,
                               String description,
                               String targetSystem,
                               String transportType,
                               String requestRef) {

        super(host, user, password);

        checkArgument(! isBlank(description), "No description provided. Cannot create transports without description.");
        checkArgument(! isBlank(targetSystem), "No target system provided. Cannot create transport without target system.");
        checkArgument(! isBlank(transportType), "No transport type provided. Cannot create transport without transport type");

        this.description = description.trim();
        this.targetSystem = targetSystem.trim();
        this.transportType = transportType.trim();

        this.owner = isBlank(owner) ? user.trim() : owner.trim();
        this.requestRef = requestRef == null ? "" : requestRef.trim();
    }

    public final static void main(String[] args) throws Exception {

        Options options = new Options();

        Command.addOpts(options);
        Opts.addOpts(options);

        if(helpRequested(args)) {
            handleHelpOption(format("%s [SPECIFIC OPTIONS]", getCommandName(CreateTransportABAP.class)),
            "Creates a new transport entity. " +
            "Returns the ID of the new transport entity. ",
            Opts.addOpts(new Options()));
            return;
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

        Transport transport = client.createTransport(Transport.getTransportCreationRequestMap(owner, description, targetSystem, requestRef, transportType));

        logger.debug(format("Transport '%s' created. isModifiable: '%b', Owner: '%s', Description: '%s'.",
            transport.getTransportID(), transport.isModifiable(), transport.getOwner(), transport.getDescription()));

        System.out.println(transport.getTransportID());
        System.out.flush();
    }
}
