package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

public class CommandsTest extends CMTestBase {

    @Test
    public void testGetVersionLongOption() throws Exception {
        /*
         * Here we depend on a maven build. Before executing this test in
         * an IDE mvn process-resources needs to be invoked.
         */
        File version = new File("target/classes/version");
        Assume.assumeTrue(version.isFile());

        Commands.main(new String[] {"--version"});

        versionAsserts(version);
    }

    @Test
    public void testGetVersionShortOption() throws Exception {
        /*
         * Here we depend on a maven build. Before executing this test in
         * an IDE mvn process-resources needs to be invoked.
         */
        File version = new File("target/classes/version");
        Assume.assumeTrue(version.isFile());

        Commands.main(new String[] {"-v"});

        versionAsserts(version);
    }


    @Test
    public void testPrintVersionWithSubcommand() throws Exception {
        /*
         * Here we depend on a maven build. Before executing this test in
         * an IDE mvn process-resources needs to be invoked.
         */
        File version = new File("../lib/target/classes/VERSION"); // not so nice, we go outside the submodule folder (../lib)
        Assume.assumeTrue(version.isFile());

        Commands.main(new String[] {"--version", "is-transport-modifiable"});

        versionAsserts(version);
    }
    private void versionAsserts(File versionFile) throws Exception {
        Properties vProps = new Properties();
        vProps.load(new FileInputStream(versionFile));
        String theVersion = format("%s : %s", vProps.getProperty("mvnProjectVersion"), vProps.getProperty("gitCommitId"));
        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")),
                is(equalTo(theVersion)));
    }


    @Test
    public void testGetGlobalHelpShortOption() throws Exception {

        Commands.main(new String[] {"-h"});
        globalHelpAssert(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")));
    }

    @Test
    public void testGetGlobalHelpLongOption() throws Exception {

        Commands.main(new String[] {"--help"});
        globalHelpAssert(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")));
    }

    @Test
    public void testPrintHelpWithSubcommandHelpBeforeCommand() throws Exception {
        Commands.main(new String[] {"--help", "is-change-in-development"});
        String help = IOUtils.toString(result.toByteArray(), "UTF-8");
        assertThat(help, Matchers.containsString("usage: <CMD> [COMMON_OPTIONS] is-change-in-development"));
    }

    @Test
    public void testPrintHelpWithSubcommandHelpAfterCommand() throws Exception {
        Commands.main(new String[] {"is-change-in-development", "--help"});
        String help = IOUtils.toString(result.toByteArray(), "UTF-8");
        assertThat(help, Matchers.containsString("usage: <CMD> [COMMON_OPTIONS] is-change-in-development"));
    }

    @Test
    public void testPrintHelp() throws Exception {
        Commands.main(new String[] {"--help"});
        String help = IOUtils.toString(result.toByteArray(), "UTF-8");
        assertThat(help, Matchers.containsString("Prints this help."));
    }

    private void globalHelpAssert(String helpOutput) {
        assertThat(helpOutput, containsString("usage: <CMD> [COMMON_OPTIONS...] <subcommand> [SUBCOMMAND_OPTIONS]")); //<parameters...> too long ..., linebreak.
        assertThat(helpOutput, containsString("Subcommands:"));
        assertThat(helpOutput, containsString("Type '<CMD> <subcommand> --help' for more details."));
    }

    @Test
    public void testGetCommandHelpLongOption() throws Exception {

        Commands.main(new String[] {"is-change-in-development", "--help"});
        commandHelpAssert();
    }

    @Test
    public void testGetCommandHelpShortOption() throws Exception {

        Commands.main(new String[] {"is-change-in-development", "-h"});
        commandHelpAssert();
    }
    private void commandHelpAssert() throws Exception {
        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), containsString("usage"));
    }

    @Test
    public void testExecuteNotExistingCommand() throws Exception {
        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Command 'does-not-exist' not found.");
        Commands.main(new String[] {"does-not-exist"});
    }

    @Test
    public void testExecuteWithOptionsBeforeSubcommandName() throws Exception {
        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Command 'does-not-exist' not found.");
        Commands.main(new String[] {"-e", "https://www.example.org/mypath",
                                    "-u", "nobody",
                                    "-p", "secret",
                                    "does-not-exist"});
    }

    @Test
    public void testExecuteWithOptionsAndWithoutSubcommandName() throws Exception {
        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Canmnot extract command name from arguments");
        Commands.main(new String[] {"-e", "https://www.example.org/mypath",
                                    "-u", "nobody",
                                    "-p", "secret"});
    }

    @Test
    public void testPrintHelpWithNotExistingSubcommand() throws Exception {
        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Command 'does-not-exist' not found.");
        Commands.main(new String[] {"--help", "does-not-exist"});
    }

    @Test
    public void testExecuteWithoutParameters() throws Exception {
        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Called without arguments.");
        Commands.main(new String[] {});
    }

    /*
     * Intended for being used with a single line string.
     */
    private static String removeCRLF(String str) {
        return str.replaceAll("\\r?\\n$", "");
    }

}
