package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

/**
 * Command for releasing a transport.
 */
@CommandDescriptor(name = "release-transport")
class ReleaseTransport extends Command {

	static class Opts {

		static Options addOptions(Options opts, boolean includeStandardOptions) {
			if (includeStandardOptions) {
				Command.addOpts(opts);
			}

			return opts.addOption(Commands.CMOptions.CHANGE_ID).addOption(TransportRelatedSOLMAN.Opts.TRANSPORT_ID);
		}
	}

	private final String changeId, transportId;

	ReleaseTransport(String host, String user, String password, String changeId, String transportId) {

		super(host, user, password);
		this.changeId = changeId;
		this.transportId = transportId;
	}

	public final static void main(String[] args) throws Exception {

		if (helpRequested(args)) {
			handleHelpOption(getCommandName(ReleaseTransport.class), "",
					"Releases the transport specified by [<changeId>,] <transportId>.",
					Opts.addOptions(new Options(), false));
			return;
		}

		CommandLine commandLine = new DefaultParser().parse(Opts.addOptions(new Options(), true), args);

		new ReleaseTransport(getHost(commandLine), getUser(commandLine), getPassword(commandLine),
				getChangeId(commandLine), TransportRelatedSOLMAN.getTransportId(commandLine)).execute();
	}

	@Override
	void execute() throws Exception {
		try (CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(host, user, password)) {
			client.releaseDevelopmentTransport(changeId, transportId);
		} catch (Exception e) {
			throw e;
		}
	}

}
