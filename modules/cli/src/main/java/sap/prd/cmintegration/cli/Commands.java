package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

class Commands {

    private final static String DASH = "-";
    private final static String TWO_DASHES = DASH+DASH;

    static class CMOptions {

        static Option USER = new Option("u", "user", true, "Service user."),
                      PASSWORD = new Option("p", "password", true, "Service password, if '-' if provided, password will be read from stdin."),
                      HOST = new Option("h", "host", true, "Service endpoint"),
                      HELP = new Option("help", "help", false, "Prints this help."),
                      VERSION = new Option("v", "version", false, "Prints the version.");

        static {
            USER.setRequired(true);
            PASSWORD.setRequired(true);
            HOST.setRequired(true);
            HELP.setRequired(false);
            VERSION.setRequired(false);
        }
    }

    static class Helpers {

        static void addStandardParameters(Options options) {
            options.addOption(CMOptions.USER);
            options.addOption(CMOptions.PASSWORD);
            options.addOption(CMOptions.HOST);
            options.addOption(CMOptions.HELP);
            options.addOption(CMOptions.VERSION);
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
            return getArg(commandLine, 0, "changeId");
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

        static void handleHelpOption(String usage, Options options) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(String.format("[options] %s", usage), options);
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
    }

    private final static Map<String, Class<? extends Command>> commands = Maps.newHashMap();

    static {
        commands.put("is-change-in-development", GetChangeStatus.class);
        commands.put("get-change-transports", GetChangeTransports.class);
        commands.put("transport-modifiable", GetTransportModifiable.class);
        commands.put("transport-owner", GetTransportOwner.class);
        commands.put("transport-description", GetTransportDescription.class);
        commands.put("upload-file-to-transport", UploadFileToTransport.class);
        commands.put("create-transport", CreateTransport.class);
        commands.put("release-transport", ReleaseTransport.class);
    }

    public final static void main(String[] args) throws Exception {
        Collection<String> _args = Arrays.asList(args);

        if((_args.contains(DASH+CMOptions.HELP.getOpt()) ||
           _args.contains(TWO_DASHES+CMOptions.HELP.getLongOpt()) &&
           args.length <= 1) || args.length == 0) {
            printHelp();
            if(args.length == 0) throw new CMCommandLineException("Called without arguments.");
            return;
        }

        if(_args.contains(DASH+CMOptions.VERSION.getOpt()) ||
           _args.contains(TWO_DASHES+CMOptions.VERSION.getLongOpt())) {
            printVersion();
            return;
        }

        final String cmdWithoutLeadingDashes = args[0].substring(TWO_DASHES.length());
        try {
            if(! commands.keySet().contains(cmdWithoutLeadingDashes)) {
                throw new CMCommandLineException(String.format("Command '%s' not found.", args[0]));
            }
            commands.get(cmdWithoutLeadingDashes).getDeclaredMethod("main", String[].class)
              .invoke(null, new Object[] { shift(args) });
        } catch (InvocationTargetException e) {
            if(e.getTargetException() instanceof Exception)
              throw (Exception)e.getTargetException();
            else
              throw e;
        }
    }

    private static void printVersion() throws IOException {
        try(InputStream version = Commands.class.getResourceAsStream("/VERSION")) {
            System.out.println(IOUtils.toString(version).replaceAll("\\r?\\n$", ""));
        }
    }

    private static void printHelp() throws Exception {

        String cmd = "cmcli";
        String CRLF = "\r\n";
        StringWriter subCommandsHelp = new StringWriter();

            commands.keySet().stream().sorted().forEach(subcmd ->
            subCommandsHelp.append(StringUtils.repeat(' ', 4))
                           .append(TWO_DASHES)
                           .append(subcmd)
                           .append(CRLF)
        );

        String cmdLineSyntax = "cmcli <subcommand> [OPTIONS...] parameters...";
        String header = "Manages communication with the SAP CM System.";
        String footer = format("Subcommands:%s%s%sType '%s <subcommand> --help' for more details.%s",
            CRLF, subCommandsHelp.toString(), CRLF, cmd, CRLF);

        new HelpFormatter().printHelp(
            cmdLineSyntax, header,
            new Options()
                .addOption(CMOptions.HELP)
                .addOption(CMOptions.VERSION),
            footer);
    }

    private static String[] shift(String[] args) {
        String[] shifted = new String[args.length - 1];
        System.arraycopy(args,  1,  shifted, 0, args.length - 1);
        return shifted;
    }
}
