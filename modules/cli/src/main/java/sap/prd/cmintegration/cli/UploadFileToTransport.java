package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getArg;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class UploadFileToTransport extends Command {

    private final String transportId, applicationId;
    private final File upload;

    public UploadFileToTransport(String host, String user, String password,
            String transportId, String applicationId, String filePath) {
        super(host, user, password);
        this.transportId = transportId;
        this.applicationId = applicationId;
        this.upload = new File(filePath);
    }

    public final static void main(String[] args) throws Exception {
        Options options = new Options();
        Commands.Helpers.addStandardParameters(options);

        if(handleHelpOption(args, "<transportId> <applicationId> <filePath>", options)) return;
        CommandLine commandLine = new DefaultParser().parse(options, args);

        new UploadFileToTransport(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getArg(commandLine, 0, "transportId"),
                getArg(commandLine, 1, "applicationId"),
                getArg(commandLine, 2, "filePath")).execute();
    }

    @Override
    void execute() throws Exception {

        if(!this.upload.canRead()) {
            throw new CMCommandLineException(String.format("Cannot read file '%s'.", upload));
        }

        ClientFactory.getInstance().newClient(host,  user, password)
            .uploadFileToTransport(transportId, upload.getAbsolutePath(), applicationId);
    }
}
