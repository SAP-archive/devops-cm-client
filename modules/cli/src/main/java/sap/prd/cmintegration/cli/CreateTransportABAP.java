package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.dto.Transport.Type;
import com.sap.cmclient.http.CMODataAbapClient;

/**
 * Command for creating a transport for a change in SAP Solution Manager.
 */
@CommandDescriptor(name = "create-transport", type = BackendType.ABAP)
class CreateTransportABAP extends Command {

    final static private Logger logger = LoggerFactory.getLogger(CreateTransportABAP.class);
    private final String owner, description, targetSystem;

    public CreateTransportABAP(String host, String user, String password, String owner, String description, String targetSystem) {
        super(host, user, password);
        this.owner = owner;
        this.description = description;
        this.targetSystem = targetSystem;
    }

    public final static void main(String[] args) throws Exception {

        logger.debug(format("%s called with arguments: '%s'.", CreateTransportABAP.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);
        options.addOption(Commands.CMOptions.CHANGE_ID);

        Option owner = new Option("o", "owner", true, "The transport owner. If ommited the login user us used."),
               description = new Option("d", "description", true, "The description of the transport request."),
               targetSystem = new Option("ts", "target-system", true, "The target of the transport");

        options.addOption(owner).addOption(description);

        if(helpRequested(args)) {
            handleHelpOption(format("%s [--owner <owner>][--description <description>] -cID <changeId>", getCommandName(CreateTransportABAP.class)),
            "Creates a new transport entity. " +
            "Returns the ID of the transport entity. " +
            "If there is already an open transport, the ID of the already existing open transport might be returned.",
            new Options().addOption(owner).addOption(description).addOption(targetSystem)); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new CreateTransportABAP(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                commandLine.getOptionValue(owner.getOpt()),
                commandLine.getOptionValue(description.getOpt()),
                commandLine.getOptionValue(targetSystem.getOpt())).execute();
    }

    @Override
    void execute() throws Exception {

        CMODataAbapClient client = AbapClientFactory.getInstance().newClient(host, user, password);

        logger.debug("Creating transport request.");

        String o = isBlank(owner) ? user : owner;

        if(isBlank(description)) {
            throw new CMCommandLineException("No description provided. Cannot create transports without description.");
        }

        GregorianCalendar date = new GregorianCalendar();
        GregorianCalendar time = new GregorianCalendar(0, 0, 0, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), date.get(Calendar.SECOND));

        Transport transport = client.createTransport(Transport.getTransportCreationRequestMap(o, description, targetSystem, "", Type.W));

        logger.debug(format("Transport '%s' created. isModifiable: '%b', Owner: '%s', Description: '%s'.",
            transport.getTransportID(), transport.isModifiable(), transport.getOwner(), transport.getDescription()));

        System.out.println(transport.getTransportID());
        System.out.flush();
    }
}
