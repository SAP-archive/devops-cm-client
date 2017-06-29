package sap.prd.cmintegration.cli;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;

public class Commands {

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
            try {
                return getArg(commandLine, 0);
            } catch(ArrayIndexOutOfBoundsException ex) {
                throw new CMCommandLineException("No changeId specified.", ex);
            }
        }

        static String getArg(CommandLine commandLine, int index) throws ArrayIndexOutOfBoundsException {
            return commandLine.getArgs()[index];
        }

        /**
         * @return <code>true</code> when the help option was detected and handled.
         */
        static boolean handleHelpOption(String[] args, Options options) {
            if(!asList(args).contains("--help"))
                return false;
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("[options...] <changeId>", options);
            return true;
        }

        private static String readPassword() throws IOException {
            return new BufferedReader(
               new InputStreamReader(System.in, "UTF-8")).readLine();
        }
    }

    private final static Map<String, Class<? extends Command>> commands = Maps.newHashMap();

    static {
        commands.put("change-status", GetChangeStatus.class);
        commands.put("transports", GetChangeTransports.class);
        commands.put("transport-modifiable", GetTransportModifiable.class);
    }

    public final static void main(String[] args) throws Exception {
        if(Arrays.asList(args).contains("--help")) {
            printHelp();
            return;
        }

        if(Arrays.asList(args).contains("--version")) {
            printVersion();
            return;
        }

        try {
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
