package sap.prd.cmintegration.cli;

import static java.lang.String.format;

import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;

public abstract class TransportRelated extends Command {

    protected static class Opts {
        protected final static Option TRANSPORT_ID = new Option("tID", "transport-id", true, "transportID");
        static {TRANSPORT_ID.setArgName("transportID");}
    }

    protected final static Predicate<Transport> description = new Predicate<Transport>() {

        @Override
        public boolean test(Transport t) {
            String description = t.getDescription();
            if(StringUtils.isBlank(description)) {
                logger.debug(format("Description of transport '%s' is blank. Nothing will be emitted.", t.getTransportID()));
                return false;
            } else {
                logger.debug(format("Description of transport '%s' is not blank. Description '%s' will be emitted.", t.getTransportID(), t.getDescription()));
                System.out.println(description); 
                return true;}
            };
        };

    protected final static Predicate<Transport> isModifiable = new Predicate<Transport>() {

        @Override
        public boolean test(Transport t) {
            System.out.println(t.isModifiable());
            return true;
        }
    };

    protected final static Predicate<Transport> getOwner = new Predicate<Transport>() {

        @Override
        public boolean test(Transport t) {

            String owner = t.getOwner();
            if(StringUtils.isBlank(owner)) {
                logger.debug(String.format("Owner attribute for transport '%s' is blank. Nothing will be emitted.", t.getTransportID()));
                return false;
            } else {
                System.out.println(owner); 
                logger.debug(String.format("Owner '%s' has been emitted for transport '%s'.", t.getOwner(), t.getTransportID()));
                return true;}
            };
    };

    protected final String transportId;

    protected TransportRelated(String host, String user, String password, String transportId) {
        super(host, user, password);
        this.transportId = transportId;
    }

    protected final static Logger logger = LoggerFactory.getLogger(TransportRelated.class);


    protected abstract Optional<Transport> getTransport();

    protected abstract Predicate<Transport> getOutputPredicate();

    @Override
    void execute() throws Exception {

        Optional<Transport> transport = getTransport();
        if(transport.isPresent()) {
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
 
            getOutputPredicate().test(t);
        }  else {
            throw new TransportNotFoundException(transportId, format("Transport '%s' not found.", transportId));
        }
    }

    static String getTransportId(CommandLine commandLine) {
        String transportID = commandLine.getOptionValue(Opts.TRANSPORT_ID.getOpt());
        if(StringUtils.isEmpty(transportID)) {
            throw new CMCommandLineException("No transportId specified.");
        }
        return transportID;
    }
}
