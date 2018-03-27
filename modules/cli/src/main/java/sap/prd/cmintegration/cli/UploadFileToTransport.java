package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getArg;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getBackendType;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cmclient.http.CMODataAbapClient;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;
import sap.prd.cmintegration.cli.TransportRelated.Opts;
import sap.prd.cmintegration.cli.TransportRetriever.BackendType;

/**
 * Command for uploading a file into a transport.
 */
@CommandDescriptor(name="upload-file-to-transport", type = BackendType.SOLMAN)
class UploadFileToTransport extends Command {

    final static private Logger logger = LoggerFactory.getLogger(TransportRelated.class);
    private final String changeId, transportId, applicationId;
    private final File upload;

    UploadFileToTransport(BackendType backendType, String host, String user, String password,
            String changeId, String transportId, String applicationId, String filePath) {
        super(backendType, host, user, password);
        this.changeId = changeId;
        this.transportId = transportId;
        this.applicationId = applicationId;
        this.upload = new File(filePath);
    }

    public final static void main(String[] args) throws Exception {
        logger.debug(format("%s called with arguments: '%s'.", UploadFileToTransport.class.getSimpleName(), Commands.Helpers.getArgsLogString(args)));
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);
        options.addOption(Commands.CMOptions.CHANGE_ID);
        options.addOption(Opts.TRANSPORT_ID);

        if(helpRequested(args)) {
            handleHelpOption(format("%s [-cID <changeId>] -tID <transportId> <applicationId> <filePath>", getCommandName(UploadFileToTransport.class)),
                    "Uploads the file specified by <filePath> to transport <transportId> [for change <changeId>]. ChangeId must not be provided for ABAP backends. "
                    + "<applicationId> specifies how the file needs to be handled on server side. In case of an ABAP backend the URL of the uploaded file is echoed to stdout.", new Options()); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        BackendType backendType = getBackendType(commandLine);

        new UploadFileToTransport(
                backendType,
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(backendType, commandLine),
                TransportRelated.getTransportId(commandLine),
                getApplicationId(backendType, commandLine),
                getFilePath(backendType, commandLine)).execute();
    }

    static String getApplicationId(BackendType type, CommandLine commandLine) {
        return (type == BackendType.ABAP) ? null : getArg(commandLine, 1, "applicationId");
    }

    static String getFilePath(BackendType type, CommandLine commandLine) {
        int index;
        if(type == BackendType.ABAP) {
            index = 1;
        } else if (type == BackendType.SOLMAN) {
            index = 2;
        } else {
            throw new IllegalArgumentException(String.format("Invalid backend type: '%s'.", type));
        }
        return getArg(commandLine, index, "filePath");
    }

    @Override
    void execute() throws Exception {

        if(!this.upload.canRead()) {
            throw new CMCommandLineException(String.format("Cannot read file '%s'.", upload));
        }

        if(type == BackendType.SOLMAN) {
            try (CMODataSolmanClient client = SolmanClientFactory.getInstance().newClient(host, user, password)) {

                logger.debug(format("Uploading file '%s' to transport '%s' for change document '%s' with applicationId '%s'.",
                        upload.getAbsolutePath(), transportId, changeId, applicationId));

                client.uploadFileToTransport(changeId, transportId, upload.getAbsolutePath(), applicationId);

                logger.debug(format("File '%s' uploaded to transport '%s' for change document '%s' with applicationId '%s'.",
                        upload.getAbsolutePath(), transportId, changeId, applicationId));
            } catch(Exception e) {
                logger.error(format("Exception caught while uploading file '%s' to transport '%s' for change document '%s' with applicationId '%s'",
                        upload.getAbsolutePath(), transportId, changeId, applicationId));
                throw new ExitException(e, 1);
            }

        } else if(type == BackendType.ABAP) {

            try {

                logger.debug(format("Uploading file '%s' to transport '%s'.",
                        upload.getAbsolutePath(), transportId));

                String location = new CMODataAbapClient(host, user, password).upload(transportId, upload);
                location += "/$value";

                System.out.println(location);

                logger.debug(format("File '%s' uploaded to transport '%s'. The file can be accessed via '%s'.",
                        upload.getAbsolutePath(), transportId, location));
            } catch(Exception e) {
                logger.error(format("Exception caught while uploading file '%s' to transport '%s'",
                        upload.getAbsolutePath(), transportId));
                throw new ExitException(e, 1);
            }
        } else {
            throw new IllegalArgumentException(format("Invalid backend type: '%s'.", type));
        }
    }
}
