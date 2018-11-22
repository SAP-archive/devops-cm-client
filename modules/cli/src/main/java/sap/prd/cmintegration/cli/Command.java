package sap.prd.cmintegration.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.cli.Options;

/**
 * Root class for all commands.
 */
abstract class Command {

    protected final String host, user, password;

    protected Command(String host, String user, String password) {

        checkArgument(! isBlank(host), "No endpoint provided.");
        checkArgument(! isBlank(user), "No user provided.");
        checkArgument(! isBlank(password), "No password provided.");

        this.host = host;
        this.user = user;
        this.password = password;
    }

    /**
     * Contains the command specific logic. E.g. performs a
     * call to SAP Solution Manager, parses the result and
     * provides it via System.out to the caller of the command line.
     * @throws Exception In case of trouble.
     */
    abstract void execute() throws Exception;

    protected static Options addOpts(Options options) {
        Commands.Helpers.addStandardParameters(options);
        return options;
    }
}
