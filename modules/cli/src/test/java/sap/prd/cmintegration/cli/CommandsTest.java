package sap.prd.cmintegration.cli;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
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

    private void versionAsserts(File versionFile) throws Exception {
        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")),
                is(equalTo(removeCRLF(FileUtils.readFileToString(versionFile)))));
    }

    @Test
    public void testExecuteNotExistingCommand() throws Exception {
        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("Command 'does-not-exist' not found.");
        Commands.main(new String[] {"does-not-exist"});
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
