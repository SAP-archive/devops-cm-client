package sap.prd.cmintegration.cli;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.google.common.collect.Maps;

public class Command {

    private final static Map<String, Class<?>> commands = Maps.newHashMap();

    static {
        commands.put("change-status", GetChangeStatus.class);
        commands.put("transports", GetChangeTransports.class);
    }

    public final static void main(String[] args) throws Exception {
        if(args.length == 1 && args[0].equals("--help")) {
            printHelp();
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

    private static void printHelp() throws Exception {
        PrintStream ps = new PrintStream(System.out);
        for(Map.Entry<String, Class<?>> e : commands.entrySet()) {
            ps.print(e.getKey() + ":: ");
            ps.print(e.getValue().getDeclaredMethod("main", String[].class).invoke(null, new Object[] {new String[] {"--help"}} ));
        }
    }

    private static String[] shift(String[] args) {
        String[] shifted = new String[args.length - 1];
        System.arraycopy(args,  1,  shifted, 0, args.length - 1);
        return shifted;
    }
}
