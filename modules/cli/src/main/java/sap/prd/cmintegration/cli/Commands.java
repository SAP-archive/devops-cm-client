package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static sap.prd.cmintegration.cli.Commands.Helpers.getArgsLogString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.commons.api.ex.ODataError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

class Commands {

    final static private Logger logger = LoggerFactory.getLogger(Commands.class);
    private final static String DASH = "-";
    private final static String TWO_DASHES = DASH+DASH;

    static class CMOptions {

        static Option USER = new Option("u", "user", true, "Service user."),
                      PASSWORD = new Option("p", "password", true, "Service password, if '-' if provided, password will be read from stdin."),
                      HOST = new Option("e", "endpoint", true, "Service endpoint"),
                      HELP = new Option("h", "help", false, "Prints this help."),
                      VERSION = new Option("v", "version", false, "Prints the version.");

        static {
            USER.setRequired(true);
            PASSWORD.setRequired(true);
            HOST.setRequired(true);
            HELP.setRequired(false);
            VERSION.setRequired(false);
        }

        static Option clone(Option o) {
            return new Option(o.getOpt(), o.getLongOpt(), o.hasArg(), o.getDescription());
        }
    }

    static class Helpers {

        static Options getStandardOptions() {
            return addStandardParameters(new Options());
        }

        static Options addStandardParameters(Options options) {
            options.addOption(CMOptions.USER);
            options.addOption(CMOptions.PASSWORD);
            options.addOption(CMOptions.HOST);
            options.addOption(CMOptions.HELP);
            options.addOption(CMOptions.VERSION);
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

        static String getHost(CommandLine commandLine) {
            return commandLine.getOptionValue(CMOptions.HOST.getOpt());
        }

        static String getChangeId(CommandLine commandLine) {
            return getArg(commandLine, 1, "changeId");
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
            return asList(args).contains("--help");
        }

        static void handleHelpOption(String usage, String header, Options options) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("<CMD> [COMMON_OPTIONS] "+ usage, header, options, "");
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
         * 
         * @return A copy of <code>args</code>. The password parameter, identified by a preceding
         *         <quote>-p</quote> is replaced by asterisks.
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
        commands.add(GetTransportModifiable.class);
        commands.add(GetTransportOwner.class);
        commands.add(GetTransportDescription.class);
        commands.add(UploadFileToTransport.class);
        commands.add(CreateTransport.class);
        commands.add(ReleaseTransport.class);

        if(commands.stream()
                .filter(it -> it.getAnnotation(CommandDescriptor.class) == null)
                .findAny()
                .isPresent()) throw new IllegalStateException(format("Command without %s annotation found.", CommandDescriptor.class.getSimpleName()));
    }

    public final static void main(String[] args) throws Exception {

        logger.debug(format("CM Client has been called with command line '%s'.", getArgsLogString(args)));
        Collection<String> _args = Arrays.asList(args);

        if((_args.contains(DASH+CMOptions.HELP.getOpt()) ||
           _args.contains(TWO_DASHES+CMOptions.HELP.getLongOpt()) &&
           args.length <= 1) || args.length == 0) {
            logger.debug("Printing help and return.");
            printHelp();
            if(args.length == 0) throw new CMCommandLineException("Called without arguments.");
            return;
        }

        if(_args.contains(DASH+CMOptions.VERSION.getOpt()) ||
           _args.contains(TWO_DASHES+CMOptions.VERSION.getLongOpt())) {
            logger.debug("Printing version and return.");
            printVersion();
            return;
        }

        final String commandName = getCommandName(args);
        try {
            Optional<Class<? extends Command>> command = commands.stream()
                .filter( it -> it.getAnnotation(CommandDescriptor.class)
                        .name().equals(commandName)).findFirst();

            if(command.isPresent()) {
                logger.debug(format("Command name '%s' resolved to implementing class '%s'.", commandName, command.get().getName()));
                command.get().getDeclaredMethod("main", String[].class)
                .invoke(null, new Object[] { args });
            } else {
                throw new CMCommandLineException(String.format("Command '%s' not found.", commandName));
            }

        } catch (InvocationTargetException e) {
            logger.error(format("Exception caught while executingn command '%s': '%s'.", commandName, e.getMessage()),e);
            if(e.getTargetException() instanceof ODataClientErrorException) {
                 StatusLine statusLine = ((ODataClientErrorException) e.getTargetException()).getStatusLine();
                 if(statusLine.getStatusCode() == 401) { // unauthorized
                     throw new ExitException(e.getTargetException(), 2);
                 } else {
                     throw (ODataClientErrorException)e.getTargetException();
                 }
            } else if(e.getTargetException() instanceof Exception)
              throw (Exception)e.getTargetException();
            else
              throw e;
        } catch(Exception e) {
            logger.error(format("Exception caught while executingn command '%s': '%s'.", commandName, e.getMessage()),e);
            throw e;
        }
    }

    private static String getCommandName(String[] args) throws ParseException {

        Options opts = new Options();
        asList(CMOptions.HELP, CMOptions.VERSION, CMOptions.HOST, CMOptions.USER, CMOptions.PASSWORD).stream().map(
           new Function<Option, Option>() {

              @Override
              public Option apply(Option o) {
                Option c = CMOptions.clone(o);
                c.setRequired(false);
                return c;
              }
          }).forEach(o -> opts.addOption(o));

          CommandLine parser = new DefaultParser().parse(opts, args, true);
          if(parser.getArgs().length == 0) {
              throw new CMCommandLineException(format("Canmnot extract command name from arguments: '%s'.",
                        getArgsLogString(args)));
          }
          String commandName = parser.getArgs()[0];
          logger.debug(format("Command name '%s' extracted from command line '%s'.", commandName, getArgsLogString(args)));
          return commandName;
    }

    private static void printVersion() throws IOException {
        System.out.println(CMODataClient.getLongVersion());
    }

    private static void printHelp() throws Exception {

        String cmd = "<CMD>";
        String CRLF = "\r\n";
        StringWriter subCommandsHelp = new StringWriter();

            commands.stream().map(it -> it.getAnnotation(CommandDescriptor.class).name())
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
