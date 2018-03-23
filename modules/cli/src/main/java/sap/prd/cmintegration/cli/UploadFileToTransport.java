package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static sap.prd.cmintegration.cli.Commands.Helpers.getArg;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
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

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.prd.cmintegration.cli.TransportRelated.Opts;

/**
 * Command for uploading a file into a transport.
 */
@CommandDescriptor(name="upload-file-to-transport")
class UploadFileToTransport extends Command {

    final static private Logger logger = LoggerFactory.getLogger(TransportRelated.class);
    private final String changeId, transportId, applicationId;
    private final File upload;

    UploadFileToTransport(String host, String user, String password,
            String changeId, String transportId, String applicationId, String filePath) {
        super(host, user, password);
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
            handleHelpOption(format("%s -cID <changeId> -tID <transportId> <applicationId> <filePath>", getCommandName(UploadFileToTransport.class)),
                    "Uploads the file specified by <filePath> to transport <transportId> for change <changeId>. "
                    + "<applicationId> specifies how the file needs to be handled on server side.", new Options()); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new UploadFileToTransport(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                TransportRelated.getTransportId(commandLine),
                getArg(commandLine, 1, "applicationId"),
                getArg(commandLine, 2, "filePath")).execute();
    }

    @Override
    void execute() throws Exception {

        if(!this.upload.canRead()) {
            throw new CMCommandLineException(String.format("Cannot read file '%s'.", upload));
        }

        try (CMODataClient client = ClientFactory.getInstance().newClient(host, user, password)) {

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
    }
}
