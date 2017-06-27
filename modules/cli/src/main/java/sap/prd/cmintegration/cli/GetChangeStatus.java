package sap.prd.cmintegration.cli;

import static sap.prd.cmintegration.cli.Command.Helpers.getHost;
import static sap.prd.cmintegration.cli.Command.Helpers.getUser;
import static sap.prd.cmintegration.cli.Command.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Command.Helpers.getChangeId;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;

public class GetChangeStatus {

    private String changeId;
    private String user;
    private String password;
    private String host;

    GetChangeStatus(String host, String user, String password, String changeId) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.changeId = changeId;
    }

    void execute() throws Exception {
        CMODataChange change = ClientFactory.getInstance().newClient(host, user, password).getChange(changeId);
        String status = change.getStatus();
        System.out.println(status);
    }

    public final static void main(String[] args) throws Exception {

        Options options = new Options();
        Command.Helpers.addStandardParameters(options);

        if(args.length >= 1 && args[0].equals("--help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("[options...] <changeId>", options);
            return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new GetChangeStatus(
                getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine)).execute();
    }
}
