package sap.prd.cmintegration.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.Helpers.getArg;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.io.File;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.Transport;

/**
 * Command for uploading a file into a transport.
 */
@CommandDescriptor(name="upload-file-to-transport", type = BackendType.ABAP)
class UploadFileToTransportABAP extends TransportRelatedABAP {

    final static private Logger logger = LoggerFactory.getLogger(TransportRelatedSOLMAN.class);
    private final File upload;

    UploadFileToTransportABAP(String host, String user, String password,
            String transportId, String filePath) {

        super(host, user, password, transportId);

        checkArgument(! isBlank(filePath), "Upload file not provided.");
        this.upload = new File(filePath);

        checkArgument(this.upload.canRead(), format("Cannot read upload file '%s'.", this.upload));
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", UploadFileToTransportABAP.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));


        if(helpRequested(args)) {
            handleHelpOption(getCommandName(UploadFileToTransportABAP.class),
                    "Uploads the file specified by <filePath> to the given transport. The URL of the uploaded file is echoed to stdout.", "<filePath>",
                    TransportRelated.Opts.addOpts(new Options(), false));
            return;
        }

        CommandLine commandLine = new DefaultParser().parse(TransportRelated.Opts.addOpts(new Options(), true), args);

        new UploadFileToTransportABAP(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getTransportId(commandLine),
                getFilePath(commandLine)).execute();
    }

    static String getFilePath(CommandLine commandLine) {
        return getArg(commandLine, 1, "filePath");
    }

    @Override
    protected Function<Transport, String> getAction() {

        return new Function<Transport, String>() {

            @Override
            public String apply(Transport t) {
                try {
                    logger.debug(format("Uploading file '%s' to transport '%s'.",
                            upload.getAbsolutePath(), transportId));

                    String location = AbapClientFactory.getInstance().newClient(host, user, password).upload(transportId, upload);
                    location += "/$value";

                    logger.debug(format("File '%s' uploaded to transport '%s'. The file can be accessed via '%s'.",
                                upload.getAbsolutePath(), transportId, location));

                    return location;
                } catch(Exception e) {
                    logger.error(format("Exception caught while uploading file '%s' to transport '%s'",
                            upload.getAbsolutePath(), transportId));
                    throw new ExitException(e, 1);
                }
            }
        };
    }
}
