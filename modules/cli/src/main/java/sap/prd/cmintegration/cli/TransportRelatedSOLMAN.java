package sap.prd.cmintegration.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.CMOptions.newOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

abstract class TransportRelatedSOLMAN extends Command {

	private final boolean returnCodeMode;
	protected final String transportId;
	protected final String changeId;

	protected final static Logger logger = LoggerFactory.getLogger(TransportRelatedSOLMAN.class);

	static class Opts {
		protected final static Option TRANSPORT_ID = newOption("tID", "transport-id", "transportID.", "tId", true);

		static Options addOptions(Options opts, boolean includeStandardOpts) {
			if (includeStandardOpts) {
				Command.addOpts(opts);
			}
			opts.addOption(TRANSPORT_ID);

			opts.addOption(Commands.CMOptions.CHANGE_ID);
			return opts;
		}
	}

	protected TransportRelatedSOLMAN(String host, String user, String password, String changeId, String transportId,
			boolean returnCodeMode) {

		super(host, user, password);
		checkArgument(!isBlank(transportId), "No transportId provided.");
		this.transportId = transportId;
		this.returnCodeMode = returnCodeMode;

		checkArgument(!isBlank(changeId), "No changeId provided.");
		this.changeId = changeId;
	}

	private static class FollowUp {
		private final static Function<String, Void> printToStdout = new Function<String, Void>() {

			@Override
			public Void apply(String output) {
				if (output != null)
					System.out.println(output);
				return null;
			}
		}, raiseFriendlyExitException = new Function<String, Void>() {

			// yes, this is some kind of miss-use of exceptions.

			@Override
			public Void apply(String output) {
				if (output != null && !Boolean.valueOf(output))
					throw new ExitException(ExitException.ExitCodes.FALSE);
				return null;
			}
		};
	}

	protected final static Function<Transport, String> getDescription = new Function<Transport, String>() {

		@Override
		public String apply(Transport t) {
			String description = t.getDescription();
			if (StringUtils.isBlank(description)) {
				logger.debug(
						format("Description of transport '%s' is blank. Nothing will be emitted.", t.getTransportID()));
				return null;
			} else {
				logger.debug(format("Description of transport '%s' is not blank. Description '%s' will be emitted.",
						t.getTransportID(), t.getDescription()));
				return description;
			}
		};
	};

	protected final static Function<Transport, String> isModifiable = new Function<Transport, String>() {

		@Override
		public String apply(Transport t) {
			return String.valueOf(t.isModifiable());
		}
	};

	protected abstract Function<Transport, String> getAction();

	protected final static Function<Transport, String> getOwner = new Function<Transport, String>() {

		@Override
		public String apply(Transport t) {

			String owner = t.getOwner();
			if (StringUtils.isBlank(owner)) {
				logger.debug(String.format("Owner attribute for transport '%s' is blank. Nothing will be emitted.",
						t.getTransportID()));
				return null;
			} else {
				logger.debug(String.format("Owner '%s' has been emitted for transport '%s'.", t.getOwner(),
						t.getTransportID()));
				return owner;
			}
		};
	};

	static String getTransportId(CommandLine commandLine) {
		String transportID = commandLine.getOptionValue(Opts.TRANSPORT_ID.getOpt());
		if (StringUtils.isEmpty(transportID)) {
			throw new CMCommandLineException("No transportId specified.");
		}
		return transportID;
	}

	protected static boolean isReturnCodeMode(CommandLine commandLine) {
		return commandLine.hasOption(Commands.CMOptions.RETURN_CODE.getOpt());
	}

	protected static void main(Class<? extends TransportRelatedSOLMAN> clazz, Options options, String[] args,
			String subCommandName, String argumentDocu, String helpText) throws Exception {

		logger.debug(
				format("%s called with arguments: %s", clazz.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

		if (helpRequested(args)) {
			handleHelpOption(subCommandName, argumentDocu, helpText,
					TransportRelatedSOLMAN.Opts.addOptions(new Options(), false));
			return;
		}

		TransportRelatedSOLMAN.Opts.addOptions(options, true);
		CommandLine commandLine = new DefaultParser().parse(options, args);

		newInstance(clazz, getHost(commandLine), getUser(commandLine), getPassword(commandLine),
				getChangeId(commandLine), getTransportId(commandLine), isReturnCodeMode(commandLine)).execute();
	}

	protected void execute() throws Exception {
		try {
			Optional<Transport> transport = getTransport();
			if (!transport.isPresent()) {
				throw new TransportNotFoundException(transportId, format("Transport '%s' not found.", transportId));
			}

			Transport t = transport.get();

			if (!t.getTransportID().trim().equals(transportId.trim())) {
				throw new CMCommandLineException(
						format("TransportId of resolved transport ('%s') does not match requested transport id ('%s').",
								t.getTransportID(), transportId));
			}

			logger.debug(format("Transport '%s' has been found. isModifiable: '%b', Owner: '%s', Description: '%s'.",
					transportId, t.isModifiable(), t.getOwner(), t.getDescription()));

			getAction().andThen(returnCodeMode ? FollowUp.raiseFriendlyExitException : FollowUp.printToStdout).apply(t);
		} catch (TransportNotFoundException e) {
			throw new CMCommandLineException(
					format("Transport '%s' not found for change '%s'.", e.getTransportId(), changeId), e);
		}
	}

	private static TransportRelatedSOLMAN newInstance(Class<? extends TransportRelatedSOLMAN> clazz, String host,
			String user, String password, String changeId, String transportId, boolean returnCodeMode) {
		try {
			return clazz.getDeclaredConstructor(
					new Class[] { String.class, String.class, String.class, String.class, String.class, Boolean.TYPE })
					.newInstance(new Object[] { host, user, password, changeId, transportId, returnCodeMode });
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException
				| InvocationTargetException e) {
			throw new RuntimeException(format("Cannot instanciate class '%s'.", clazz.getName()), e);
		}
	}

	protected Optional<Transport> getTransport() {
		try (CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(host, user, password)) {
			return client.getChangeTransports(changeId).stream().filter(it -> it.getTransportID().equals(transportId))
					.findFirst();
		}
	}
}
