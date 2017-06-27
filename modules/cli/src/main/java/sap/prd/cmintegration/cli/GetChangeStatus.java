package sap.prd.cmintegration.cli;

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

        String host = Command.Helpers.getHost(commandLine);
        String user = Command.Helpers.getUser(commandLine);

        String password = Command.Helpers.getPassword(commandLine);

        String changeId = Command.Helpers.getChangeId(commandLine);

        new GetChangeStatus(host, user, password, changeId).execute();
    }
}
