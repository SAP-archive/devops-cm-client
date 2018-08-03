package sap.prd.cmintegration.cli;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static sap.prd.cmintegration.cli.Commands.Helpers.getArgsLogString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.sap.cmclient.http.UnexpectedHttpResponseException;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataSolmanClient;

/**
 * Helpers for using/calling commands.
 */
class Commands {

    final static private Logger logger = LoggerFactory.getLogger(Commands.class);
    private final static String DASH = "-";

    /**
     * The common command line options used by all commands.
     */
    static class CMOptions {

        static Option USER = newOption("u", "user", "Service user.", "user", true),
                      PASSWORD = newOption("p", "password", "Service password, if '-' is provided, password will be read from stdin.", "pwd", true),
                      BACKEND_TYPE = newOption("t", "backend-type", format("Backend Type, one of %s.", asList(BackendType.values())), "type", true),
                      HOST = newOption("e", "endpoint", "Service endpoint.", "url", true),
                      HELP = newOption("h", "help", "Prints this help.", null, false),
                      VERSION = newOption("v", "version", "Prints the version.", null, false),

                      CHANGE_ID = newOption("cID", "change-id", "changeID.", "cID", false),
                      DEVELOPMENT_SYSTEM_ID = newOption("dID", "development-system-id", "DevelopmentSystemID", "devSysID", false),

                      RETURN_CODE = newOption("rc", "return-code",
                          format("If used with this option return code is %s " +
                          "in case of a modifiable transport and %d in case " +
                          "the transport is not modifiable. In this mode nothing is " +
                          "emitted to STDOUT.", ExitException.ExitCodes.OK, ExitException.ExitCodes.FALSE), null, false);

        static Option newOption(String shortKey, String longKey, String desc, String argName, boolean required) {
            return Option.builder(shortKey)
                        .hasArg(argName != null)
                        .argName(argName)
                        .longOpt(longKey)
                        .desc(desc)
                        .required(required).build();
        }
    }

    /**
     * Common helper methods.
     */
    static class Helpers {

        static Options getStandardOptions() {
            return addStandardParameters(new Options());
        }

        static Options addStandardParameters(Options o) {
            return getStandardParameters(o, false);
        }

        static Options getStandardParameters(boolean optional) {
            return getStandardParameters(new Options(), optional);
        }

        static Options getStandardParameters(Options options, boolean optional) {
            Set<Option> standardOpts = newHashSet(
              CMOptions.USER,
              CMOptions.PASSWORD,
              CMOptions.BACKEND_TYPE,
              CMOptions.HOST,
              CMOptions.HELP,
              CMOptions.VERSION);

            standardOpts.stream().forEach(o -> { Option c = (Option)o.clone();
                                                 if(optional) c.setRequired(false);
                                                 options.addOption(c);});
            return options;
        }

        static String getPassword(CommandLine commandLine) throws IOException {
            String password = commandLine.getOptionValue(CMOptions.PASSWORD.getOpt());
            if(password.equals("-")) password = readPassword();
            return password;
        }

        static String getUser(CommandLine commandLine) {
            return commandLine.getOptionValue(CMOptions.USER.getOpt());
        }

        static BackendType getBackendType(CommandLine commandLine) {
            try {
                return BackendType.valueOf(commandLine.getOptionValue(CMOptions.BACKEND_TYPE.getOpt()));
            } catch(IllegalArgumentException e) {
                throw new RuntimeException("Cannot retrieve backend type.", e);
            }
        }

        static String getHost(CommandLine commandLine) {
            return commandLine.getOptionValue(CMOptions.HOST.getOpt());
        }

        static String getChangeId(CommandLine commandLine) {
            String changeID = commandLine.getOptionValue(CMOptions.CHANGE_ID.getOpt());
            if(StringUtils.isEmpty(changeID)) {
                throw new CMCommandLineException("No changeId specified.");
            }
            return changeID;
        }

        static String getDevelopmentSystemId(CommandLine commandLine) {
            String developmentSystemId = commandLine.getOptionValue(CMOptions.DEVELOPMENT_SYSTEM_ID.getOpt());
            if(StringUtils.isEmpty(developmentSystemId)) {
                throw new CMCommandLineException("No developmentSystemId specified.");
            }
            return developmentSystemId;
        }

        static String getArg(CommandLine commandLine, int index, String name) {
            try {
                return getArg(commandLine, index);
            } catch(ArrayIndexOutOfBoundsException ex) {
                throw new CMCommandLineException(format("No %s specified.", name), ex);
            }
        }

        static String getArg(CommandLine commandLine, int index) throws ArrayIndexOutOfBoundsException {
            return commandLine.getArgs()[index];
        }

        static boolean helpRequested(String[] args) {
            List<String> l = asList(args);
            return l.contains("--help") || l.contains("-h");
        }

        static void handleHelpOption(String commandName, String header, String args, Options options) {

            String exitCodes = format(
                      "    %d  The request completed successfully.\n"
                    + "    %d  The request did not complete successfully and\n"
                    + "       no more specific return code as defined below applies.\n"
                    + "    %d  Wrong credentials.\n"
                    + "    %d  Intentionally used by --return-code option in order to\n"
                    + "       indicate 'false'. Only available for commands providing\n"
                    + "       the --return-code option.",
                        ExitException.ExitCodes.OK,
                        ExitException.ExitCodes.GENERIC_FAILURE,
                        ExitException.ExitCodes.NOT_AUTHENTIFICATED,
                        ExitException.ExitCodes.FALSE);

            String commonOpts;

            HelpFormatter formatter = new HelpFormatter();

            try( StringWriter commonOptions = new StringWriter();
                 PrintWriter pw = new PrintWriter(commonOptions);) {
                formatter.printOptions(pw, formatter.getWidth(), Command.addOpts(new Options()), formatter.getLeftPadding(), formatter.getDescPadding());
                commonOpts = commonOptions.toString();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }

            String footer = format("%nCOMMON OPTIONS:%n%s%nEXIT CODES%n%s", commonOpts, exitCodes);

            formatter.printHelp(
                    format("<CMD> [COMMON_OPTIONS] %s [SUBCOMMNAD_OPTIONS] %s%n%n", 
                            commandName,
                            args != null ? args : ""),
                    format("SUBCOMMAND OPTIONS:%n",header), options, footer);

        }

        private static String readPassword() throws IOException {
            BufferedReader buff = new BufferedReader(
                    new InputStreamReader(System.in, "UTF-8"));
            String passwd = buff.readLine();
            if(passwd == null || passwd.isEmpty())
                throw new CMCommandLineException("Empty password found.");
            if(buff.readLine() != null)
                throw new CMCommandLineException("Multiline passwords are not supported.");
            return passwd;
        }

        static String getCommandName(Class<? extends Command> clazz) {
            return clazz.getAnnotation(CommandDescriptor.class).name();
        }
        
        static String getArgsLogString(String[] args) {
            return StringUtils.join(hidePassword(args), " ");
        }

        /**
         * @param args The <code>args</code> array handed over to the command.
         * @return A copy of <code>args</code>. The password parameter, identified by a preceding
         *         <code>-p</code> is replaced by asterisks.
         */
        static String[] hidePassword(String[] args) {
            String[] copy = new String[args.length];
            System.arraycopy(args, 0, copy,0, args.length);
            for(int i = 0, length = args.length; i < length; i++) {
                if(args[i].equals(DASH + CMOptions.PASSWORD.getOpt()) ||
                   args[i].equals(DASH + DASH + CMOptions.PASSWORD.getLongOpt())) {
                    if(i < args.length -1) {
                        // -p provided, but no subsequent password? We should not fail
                        // in this case with array index out of bound.
                        if(!args[i+1].equals(DASH)) copy[i+1] = "********";
                        i++; // do not check the password itself
                    }
                }
            }
            return copy;
        }
    }

    private final static Set<Class<? extends Command>> commands = Sets.newHashSet();

    static {
        commands.add(GetChangeStatus.class);
        commands.add(GetChangeTransports.class);
        commands.add(GetTransportModifiableSOLMAN.class);
        commands.add(GetTransportModifiableABAP.class);
        commands.add(GetTransportOwnerSOLMAN.class);
        commands.add(GetTransportOwnerABAP.class);
        commands.add(GetTransportDescriptionSOLMAN.class);
        commands.add(GetTransportDescriptionABAP.class);
        commands.add(UploadFileToTransportSOLMAN.class);
        commands.add(UploadFileToTransportABAP.class);
        commands.add(CreateTransportSOLMAN.class);
        commands.add(CreateTransportABAP.class);
        commands.add(ReleaseTransport.class);
        commands.add(ImportTransport.class);
        commands.add(ExportTransport.class);
        commands.add(GetTransportStatusABAP.class);
        commands.add(GetTransportTargetSystemABAP.class);
        commands.add(GetTransportTypeABAP.class);
        commands.add(GetTransportDevelopmentSystemSOLMAN.class);

        if(commands.stream()
                .filter(it -> it.getAnnotation(CommandDescriptor.class) == null)
                .findAny()
                .isPresent()) throw new IllegalStateException(format("Command without %s annotation found.", CommandDescriptor.class.getSimpleName()));
    }

    public final static void main(String[] args) throws Exception {

        logger.debug(format("CM Client has been called with command line '%s'.", getArgsLogString(args)));

        CommandLine commandLine = new DefaultParser().parse(Helpers.getStandardParameters(true), args, true);

        if((commandLine.hasOption(CMOptions.HELP.getOpt()) &&
           args.length <= 1) || args.length == 0) {
            logger.debug("Printing help and return.");
            printHelp();
            if(args.length == 0) throw new CMCommandLineException("Called without arguments.");
            return;
        }

        if(commandLine.hasOption(CMOptions.VERSION.getOpt())) {
            logger.debug("Printing version and return.");
            printVersion();
            return;
        }

        final String commandName;
        try {
            commandName = getCommandName(commandLine, args);
        } catch(CMCommandLineException e) {
            if(commandLine.hasOption(CMOptions.HELP.getOpt())) {
                printHelp();
                return;
            } else {
                throw e;
            }
        }

        final BackendType type = getBackendType(commandLine, args);
 
        try {
            Optional<Class<? extends Command>> command = commands.stream()
                .filter (it ->
                { CommandDescriptor a = it.getAnnotation(CommandDescriptor.class);
                  return a.name().equals(commandName) && a.type() == type;}).findFirst();

            if(command.isPresent()) {
                logger.debug(format("Command name '%s' resolved to implementing class '%s'.", commandName, command.get().getName()));
                command.get().getDeclaredMethod("main", String[].class)
                .invoke(null, new Object[] { args });
            } else {
                throw new CMCommandLineException(String.format("Command '%s' not found for backend type '%s'.", commandName, type));
            }
        } catch (InvocationTargetException e) {
            logger.error(format("Exception caught while executing command '%s': '%s'.", commandName, e.getMessage()),e);
            throw handle(e);
        } catch(Exception e) {
            logger.error(format("Exception caught while executing command '%s': '%s'.", commandName, e.getMessage()),e);
            throw e;
        }
    }

    private static Exception handle(InvocationTargetException e) {
        if(e == null || e.getTargetException() == null) throw new RuntimeException("No exception (?)");
        StatusLine statusLine = null;
        if(e.getTargetException() instanceof ODataClientErrorException) {
           statusLine = ((ODataClientErrorException) e.getTargetException()).getStatusLine();
       } else if(e.getTargetException() instanceof UnexpectedHttpResponseException) {
           statusLine = ((UnexpectedHttpResponseException)e.getTargetException()).getStatus();
       }

       if(statusLine != null && statusLine.getStatusCode() == 401) { // unauthorized
           return new ExitException(e.getTargetException(), 2);
       }

       return (e.getTargetException() instanceof Exception) ? (Exception)e.getTargetException() : new RuntimeException(e.getTargetException());
    }

    private static BackendType getBackendType(CommandLine commandLine, String[] args) {
        String b = commandLine.getOptionValue(CMOptions.BACKEND_TYPE.getOpt());
        if(StringUtils.isEmpty(b)) {
            printHelp();
            throw new CMCommandLineException(format("Cannot retrieve backend type. Provide common option '-%s'. Values: %s.", CMOptions.BACKEND_TYPE.getOpt(), asList(BackendType.values())));
        }
        return BackendType.valueOf(b);
    }

    private static String getCommandName(CommandLine commandLine, String[] args) throws ParseException {

        if(commandLine.getArgs().length == 0) {
            throw new CMCommandLineException(format("Canmnot extract command name from arguments: '%s'.",
                        getArgsLogString(args)));
        }

        String commandName = commandLine.getArgs()[0];
        logger.debug(format("Command name '%s' extracted from command line '%s'.", commandName, getArgsLogString(args)));
        return commandName;
    }

    private static void printVersion() throws IOException {
        System.out.println(CMODataSolmanClient.getLongVersion());
    }

    private static void printHelp() {

        String cmd = "<CMD>";
        String CRLF = "\r\n";
        StringWriter subCommandsHelp = new StringWriter();

            commands.stream().map(it -> { CommandDescriptor cDesc = it.getAnnotation(CommandDescriptor.class);
                                          return format("%s (%s)", cDesc.name(), cDesc.type());})
            .sorted().forEach(subcmd ->
            subCommandsHelp.append(StringUtils.repeat(' ', 4))
                           .append(subcmd)
                           .append(CRLF)
        );

        String cmdLineSyntax = format("%s [COMMON_OPTIONS...] <subcommand> [SUBCOMMAND_OPTIONS] <parameters...>", cmd);
        String header = "Manages communication with the SAP CM System.\r\nCOMMON OPTIONS:";
        String footer = format("Subcommands:%s%s%sType '%s <subcommand> --help' for more details.%s",
            CRLF, subCommandsHelp.toString(), CRLF, cmd, CRLF);

        new HelpFormatter().printHelp(
            cmdLineSyntax, header,
            Helpers.getStandardOptions(),
            footer);
    }
}
