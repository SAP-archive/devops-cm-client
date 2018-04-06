package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;

import java.util.function.Function;

import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;

/**
 * Command for retrieving the owner of a transport.
 */
@CommandDescriptor(name="get-transport-status", type = BackendType.ABAP)
class GetTransportStatusABAP extends TransportRelatedABAP {

    final static private Logger logger = LoggerFactory.getLogger(GetTransportStatusABAP.class);

    GetTransportStatusABAP(String host, String user, String password, String transportId) {
        super(host, user, password, transportId);
    }

    @Override
    protected Function<Transport, String> getAction() {
        return new Function<Transport, String>() {

            @Override
            public String apply(Transport t) {

                // ... ugly downcast
                String status = ((com.sap.cmclient.dto.Transport)t).getStatus();
                if(StringUtils.isBlank(status)) {
                    logger.debug(String.format("Status attribute for transport '%s' is blank. Nothing will be emitted.", t.getTransportID()));
                    return null;
                } else {
                    logger.debug(String.format("Status '%s' has been emitted for transport '%s'.", status, t.getTransportID()));
                    return status;}
                };
        };
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", GetTransportStatusABAP.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

        Options options = new Options();

        TransportRelatedABAP.main(GetTransportStatusABAP.class, options, args,
                format("%s -%s <%s>", getCommandName(GetTransportStatusABAP.class),
                Opts.TRANSPORT_ID.getOpt(), Opts.TRANSPORT_ID.getArgName()),
                format("Returns the status of the transport represented by <%s>.", Opts.TRANSPORT_ID.getArgName()));
    }
}
