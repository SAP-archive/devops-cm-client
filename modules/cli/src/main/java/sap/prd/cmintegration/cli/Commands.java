package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;

class Commands {

    static class CMOptions {

        static Option USER = new Option("u", "user", true, "Service user."),
                      PASSWORD = new Option("p", "password", true, "Service password, if '-' if provided, password will be read from stdin."),
                      HOST = new Option("h", "host", true, "Host"),
                      HELP = new Option("help", "help", false, "Prints this help.");

        static {
            USER.setRequired(true);
            PASSWORD.setRequired(true);
            HOST.setRequired(true);
            HELP.setRequired(false);
        }
    }

    static class Helpers {

        static void addStandardParameters(Options options) {
            options.addOption(CMOptions.USER);
            options.addOption(CMOptions.PASSWORD);
            options.addOption(CMOptions.HOST);
            options.addOption(CMOptions.HELP);
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
        if((_args.contains("--help") && _args.size() == 1) || _args.isEmpty()) {
            printHelp();
            if(_args.isEmpty()) throw new CMCommandLineException("Called without arguments.");
            return;
        }

        if(Arrays.asList(args).contains("--version")) {
            printVersion();
            return;
        }

        try {
            if(! commands.keySet().contains(args[0])) {
                throw new CMCommandLineException(String.format("Command '%s' not found.", args[0]));
            }
            commands.get(args[0]).getDeclaredMethod("main", String[].class)
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
        PrintStream ps = new PrintStream(System.out);
        for(Map.Entry<String, Class<? extends Command>> e : commands.entrySet()) {
            ps.print(e.getKey() + ":: ");
            e.getValue().getDeclaredMethod("main", String[].class).invoke(null, new Object[] {new String[] {"--help"}} );
        }
    }

    private static String[] shift(String[] args) {
        String[] shifted = new String[args.length - 1];
        System.arraycopy(args,  1,  shifted, 0, args.length - 1);
        return shifted;
    }
}
