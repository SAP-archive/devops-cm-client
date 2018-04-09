package sap.prd.cmintegration.cli;

import static java.lang.String.format;
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
import com.sap.cmclient.http.CMODataAbapClient;

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
        this.upload = new File(filePath);
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", UploadFileToTransportABAP.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);
        options.addOption(Opts.TRANSPORT_ID);

        if(helpRequested(args)) {
            handleHelpOption(format("%s [-cID <changeId>] -tID <transportId> <applicationId> <filePath>", getCommandName(UploadFileToTransportABAP.class)),
                    "Uploads the file specified by <filePath> to transport <transportId> [for change <changeId>]. ChangeId must not be provided for ABAP backends. "
                    + "<applicationId> specifies how the file needs to be handled on server side. In case of an ABAP backend the URL of the uploaded file is echoed to stdout.", new Options()); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new UploadFileToTransportABAP(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                TransportRelatedSOLMAN.getTransportId(commandLine),
                getFilePath(commandLine)).execute();
    }

    static String getApplicationId(BackendType type, CommandLine commandLine) {
        return (type == BackendType.ABAP) ? null : getArg(commandLine, 1, "applicationId");
    }

    static String getFilePath(CommandLine commandLine) {
        return getArg(commandLine, 1, "filePath");
    }

    @Override
    void execute() throws Exception {

        if(!this.upload.canRead()) {
            throw new CMCommandLineException(String.format("Cannot read file '%s'.", upload));
        }

        super.execute();
    }

    @Override
    protected Function<Transport, String> getAction() {

        return new Function<Transport, String>() {

            @Override
            public String apply(Transport t) {
                try {
                    logger.debug(format("Uploading file '%s' to transport '%s'.",
                            upload.getAbsolutePath(), transportId));

                    String location = new CMODataAbapClient(host, user, password).upload(transportId, upload);
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
