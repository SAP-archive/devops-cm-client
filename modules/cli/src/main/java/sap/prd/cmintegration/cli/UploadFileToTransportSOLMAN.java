package sap.prd.cmintegration.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.Helpers.getArg;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
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

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

/**
 * Command for uploading a file into a transport.
 */
@CommandDescriptor(name="upload-file-to-transport", type = BackendType.SOLMAN)
class UploadFileToTransportSOLMAN extends TransportRelatedSOLMAN {

    final static private Logger logger = LoggerFactory.getLogger(TransportRelatedSOLMAN.class);

    private final String applicationId;

    private final File upload;

    UploadFileToTransportSOLMAN(String host, String user, String password,
            String changeId, String transportId, String applicationId, String filePath) {

        super(host, user, password, changeId, transportId);

        checkArgument(! isBlank(applicationId), "applicationId was null or empty.");
        checkArgument(! isBlank(filePath), "filePath was null or empty.");

        this.applicationId = applicationId;
        this.upload = new File(filePath);

        checkArgument(this.upload.canRead(), format("Cannot read upload file '%s'.", this.upload));
    }

    public final static void main(String[] args) throws Exception {

        logger.debug(format("%s called with arguments: '%s'.", UploadFileToTransportSOLMAN.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));

        if(helpRequested(args)) {
            handleHelpOption(format("%s [SPECIFIC OPTIONS] <applicationId> <filePath>", getCommandName(UploadFileToTransportSOLMAN.class)),
                    "Uploads the file specified by <filePath> into the given transport. "
                    + "<applicationId> specifies how the file needs to be handled on server side.",
                    TransportRelated.Opts.addOpts(new Options(), false).addOption(Commands.CMOptions.CHANGE_ID));
            return;
        }

        CommandLine commandLine = new DefaultParser().parse(
                TransportRelated.Opts.addOpts(new Options(), true).addOption(Commands.CMOptions.CHANGE_ID), args);

        new UploadFileToTransportSOLMAN(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                getTransportId(commandLine),
                getApplicationId(commandLine),
                getFilePath(commandLine)).execute();
    }

    static String getApplicationId(CommandLine commandLine) {
        return getArg(commandLine, 1, "applicationId");
    }

    static String getFilePath(CommandLine commandLine) {
        return getArg(commandLine, 2, "filePath");
    }

    @Override
    protected Function<Transport, String> getAction() {

        return new Function<Transport, String>() {

            @Override
            public String apply(Transport t) {
                try (CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(host, user, password)) {

                    logger.debug(format("Uploading file '%s' to transport '%s' for change document '%s' with applicationId '%s'.",
                            upload.getAbsolutePath(), transportId, changeId, applicationId));

                    client.uploadFileToTransport(changeId, transportId, upload.getAbsolutePath(), applicationId);

                    logger.debug(format("File '%s' uploaded to transport '%s' for change document '%s' with applicationId '%s'.",
                            upload.getAbsolutePath(), transportId, changeId, applicationId));

                    return null;
                } catch(Exception e) {
                    logger.error(format("Exception caught while uploading file '%s' to transport '%s' for change document '%s' with applicationId '%s'",
                            upload.getAbsolutePath(), transportId, changeId, applicationId));
                    throw new ExitException(e, 1);
                }
            }
        };
    }
}
