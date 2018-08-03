package sap.prd.cmintegration.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.CMOptions.newOption;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;
import com.sap.cmclient.http.UnexpectedHttpResponseException;

public abstract class TransportRelated extends Command {

    private final boolean returnCodeMode;

    protected static class Opts {
        protected final static Option TRANSPORT_ID = newOption("tID", "transport-id", "transportID.", "tId", true);

        protected static Options addOpts(Options options, boolean includeStandardOpts) {
            if(includeStandardOpts) {
                Command.addOpts(options);
            }
            options.addOption(TransportRelated.Opts.TRANSPORT_ID);
            return options;
        }
    }

    private static class FollowUp {
        private final static Function<String, Void> printToStdout = new Function<String, Void>() {

            @Override
            public Void apply(String output) {
                if(output != null) System.out.println(output);
                return null;
            }
        },
        raiseFriendlyExitException = new Function<String, Void>() {

            // yes, this is some kind of miss-use of exceptions.

            @Override
            public Void apply(String output) {
                if(output != null && ! Boolean.valueOf(output))
                    throw new ExitException(ExitException.ExitCodes.FALSE);
                return null;
            }
        };
    }

    protected final static Function<Transport, String> getDescription = new Function<Transport, String>() {

        @Override
        public String apply(Transport t) {
            String description = t.getDescription();
            if(StringUtils.isBlank(description)) {
                logger.debug(format("Description of transport '%s' is blank. Nothing will be emitted.", t.getTransportID()));
                return null;
            } else {
                logger.debug(format("Description of transport '%s' is not blank. Description '%s' will be emitted.", t.getTransportID(), t.getDescription()));
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

    protected final static Function<Transport, String> getOwner = new Function<Transport, String>() {

        @Override
        public String apply(Transport t) {

            String owner = t.getOwner();
            if(StringUtils.isBlank(owner)) {
                logger.debug(String.format("Owner attribute for transport '%s' is blank. Nothing will be emitted.", t.getTransportID()));
                return null;
            } else {
                logger.debug(String.format("Owner '%s' has been emitted for transport '%s'.", t.getOwner(), t.getTransportID()));
                return owner;}
            };
    };

    protected final String transportId;

    protected TransportRelated(String host, String user, String password, String transportId, boolean returnCodeMode) {
        super(host, user, password);
        checkArgument(! isBlank(transportId), "No transportId provided.");
        this.transportId = transportId;
        this.returnCodeMode = returnCodeMode;
    }

    protected final static Logger logger = LoggerFactory.getLogger(TransportRelated.class);

    protected abstract Optional<Transport> getTransport() throws UnexpectedHttpResponseException;

    protected abstract Function<Transport, String> getAction();

    @Override
    void execute() throws Exception {

        Optional<Transport> transport = getTransport();
        if(! transport.isPresent()) {
            throw new TransportNotFoundException(transportId, format("Transport '%s' not found.", transportId));
        }

        Transport t = transport.get();

        if(!t.getTransportID().trim().equals(transportId.trim())) {
            throw new CMCommandLineException(
                format("TransportId of resolved transport ('%s') does not match requested transport id ('%s').",
                        t.getTransportID(),
                        transportId));
        }

        logger.debug(format("Transport '%s' has been found. isModifiable: '%b', Owner: '%s', Description: '%s'.",
                transportId,
                t.isModifiable(), t.getOwner(), t.getDescription()));

        getAction()
            .andThen(returnCodeMode ? FollowUp.raiseFriendlyExitException : FollowUp.printToStdout)
            .apply(t);
    }

    static String getTransportId(CommandLine commandLine) {
        String transportID = commandLine.getOptionValue(Opts.TRANSPORT_ID.getOpt());
        if(StringUtils.isEmpty(transportID)) {
            throw new CMCommandLineException("No transportId specified.");
        }
        return transportID;
    }

    protected static boolean isReturnCodeMode(CommandLine commandLine) {
        return commandLine.hasOption(Commands.CMOptions.RETURN_CODE.getOpt());
    }

}
