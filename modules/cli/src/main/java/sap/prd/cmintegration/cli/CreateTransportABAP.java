package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.Helpers.getBackendType;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
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
import com.sap.cmclient.dto.Transport.Status;
import com.sap.cmclient.dto.Transport.Type;
import com.sap.cmclient.http.CMODataAbapClient;

/**
 * Command for creating a transport for a change in SAP Solution Manager.
 */
@CommandDescriptor(name = "create-transport", type = BackendType.ABAP)
class CreateTransportABAP extends Command {

    final static private Logger logger = LoggerFactory.getLogger(CreateTransportABAP.class);
    private final String changeId, owner, description;

    public CreateTransportABAP(String host, String user, String password, String changeId,
            String owner, String description) {
        super(host, user, password);
        this.changeId = changeId;
        this.owner = owner;
        this.description = description;
    }

    public final static void main(String[] args) throws Exception {

        logger.debug(format("%s called with arguments: '%s'.", CreateTransportABAP.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);
        options.addOption(Commands.CMOptions.CHANGE_ID);

        Option owner = new Option("o", "owner", true, "The transport owner. If ommited the login user us used."),
               description = new Option("d", "description", true, "The description of the transport request.");

        options.addOption(owner).addOption(description);

        if(helpRequested(args)) {
            handleHelpOption(format("%s [--owner <owner>][--description <description>] -cID <changeId>", getCommandName(CreateTransportABAP.class)),
            "Creates a new transport entity. " +
            "Returns the ID of the transport entity. " +
            "If there is already an open transport, the ID of the already existing open transport might be returned.",
            new Options().addOption(owner).addOption(description)); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        BackendType backendType = getBackendType(commandLine);

        new CreateTransportABAP(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(backendType, commandLine),
                commandLine.getOptionValue(owner.getOpt()),
                commandLine.getOptionValue(description.getOpt())).execute();
    }

    @Override
    void execute() throws Exception {

        CMODataAbapClient client = AbapClientFactory.getInstance().newClient(host, user, password);

        logger.debug(format("Creating transport request for changeId '%s'.", changeId));

        String o = isBlank(owner) ? user : owner;

        if(isBlank(description)) {
            throw new CMCommandLineException("No description provided. Cannot create transports without description.");
        }

        GregorianCalendar date = new GregorianCalendar();
        GregorianCalendar time = new GregorianCalendar(0, 0, 0, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), date.get(Calendar.SECOND));

        Transport transport = client.createTransport(new Transport("", o, description, "A5T", date, time, "", "X", Status.D, Type.W));

        logger.debug(format("Transport '%s' created for change document '%s'. isModifiable: '%b', Owner: '%s', Description: '%s'.",
            transport.getTransportID(), changeId, transport.isModifiable(), transport.getOwner(), transport.getDescription()));

        System.out.println(transport.getTransportID());
        System.out.flush();
    }
}
