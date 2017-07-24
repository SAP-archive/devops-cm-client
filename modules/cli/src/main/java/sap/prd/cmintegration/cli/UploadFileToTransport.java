package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.getArg;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

class UploadFileToTransport extends Command {

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
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);

        if(helpRequested(args)) {
            handleHelpOption("<changeId> <transportId> <applicationId> <filePath>",
                    "Uploads the file specified by '<filePath>' to transport <transportId> for change <changeId>. "
                    + "<applicationId> specifies how the file needs to be handled on server side.", options); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new UploadFileToTransport(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getArg(commandLine, 0, "changeId"),
                getArg(commandLine, 1, "transportId"),
                getArg(commandLine, 2, "applicationId"),
                getArg(commandLine, 3, "filePath")).execute();
    }

    @Override
    void execute() throws Exception {

        if(!this.upload.canRead()) {
            throw new CMCommandLineException(String.format("Cannot read file '%s'.", upload));
        }

        try (CMODataClient client = ClientFactory.getInstance().newClient(host, user, password)) {
            client.uploadFileToTransport(changeId, transportId, upload.getAbsolutePath(), applicationId);
        } catch(Exception e) {
            throw new ExitException(e, 1);
        }
    }
}
