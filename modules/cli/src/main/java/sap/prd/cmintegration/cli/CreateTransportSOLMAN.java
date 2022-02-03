package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getDevelopmentSystemId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;
import static sap.prd.cmintegration.cli.Commands.CMOptions.newOption;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

/**
 * Command for creating a transport for a change in SAP Solution Manager.
 */
@CommandDescriptor(name = "create-transport")
class CreateTransportSOLMAN extends Command {

    static class Opts {

        static Option owner = newOption("o", "owner", "The transport owner. If ommited the login user us used.", "owner", false),
                      description = newOption("d", "description", "The description of the transport request.", "desc", false);

        static Options addOptions(Options opts, boolean addStandardOptions) {
            if(addStandardOptions) {
                Command.addOpts(opts);
            }

            return opts.addOption(Commands.CMOptions.CHANGE_ID)
                       .addOption(Commands.CMOptions.DEVELOPMENT_SYSTEM_ID)
                       .addOption(owner)
                       .addOption(description);
        }
    }

    final static private Logger logger = LoggerFactory.getLogger(CreateTransportSOLMAN.class);
    private final String changeId, developmentSystemId, owner, description;

    public CreateTransportSOLMAN(String host, String user, String password, String changeId, String developmentSystemId,
            String owner, String description) {
        super(host, user, password);
        this.changeId = changeId;
        this.owner = owner;
        this.description = description;
        this.developmentSystemId = developmentSystemId;
    }

    public final static void main(String[] args) throws Exception {

        if(helpRequested(args)) {
            handleHelpOption(getCommandName(CreateTransportSOLMAN.class), "",
            "Creates a new transport entity. " +
            "Returns the ID of the transport entity. " +
            "If there is already an open transport, the ID of the already existing open transport might be returned.",
            Opts.addOptions(new Options(), false));

            return;
        }

        CommandLine commandLine = new DefaultParser().parse(Opts.addOptions(new Options(), true), args);

        new CreateTransportSOLMAN(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                getDevelopmentSystemId(commandLine),
                commandLine.getOptionValue(Opts.owner.getOpt()),
                commandLine.getOptionValue(Opts.description.getOpt())).execute();
    }

    @Override
    void execute() throws Exception {
        try(CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(host, user,  password)) {
            logger.debug(format("Creating transport request for changeId '%s'.", changeId));

            CMODataTransport transport;
            if(owner == null && description == null) {

                transport = client.createDevelopmentTransport(changeId, developmentSystemId);

            } else {

                String d = isBlank(description) ? "" : description,
                       o = isBlank(owner) ? user : owner;

                logger.debug(format("Creating transport with owner '%s' and description '%s'", o, d));
                transport = client.createDevelopmentTransportAdvanced(
                              changeId, developmentSystemId, d, o);
            }
            logger.debug(format("Transport '%s' created for change document '%s'. isModifiable: '%b', Owner: '%s', Description: '%s'.",
                transport.getTransportID(), changeId, transport.isModifiable(), transport.getOwner(), transport.getDescription()));
            System.out.println(transport.getTransportID());
            System.out.flush();
        } catch(final Exception e) {
            logger.error(format("Exception caught while created transport request for change document '%s'.",changeId), e);
            throw e;
        }
    }
}
