package sap.prd.cmintegration.cli;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class CommandsTest extends CMTestBase {

    @Test
    public void testGetVersion() throws Exception {
        /*
         * Here we depend on a maven build. Before executing this test in
         * an IDE mvn process-resources needs to be invoked.
         */
        File version = new File("target/classes/version");
        Assume.assumeTrue(version.isFile());

        Commands.main(new String[] {"--version"});

        Assert.assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")),
                is(equalTo(removeCRLF(FileUtils.readFileToString(version)))));
    }

    /*
     * Intended for being used with a single line string.
     */
    private static String removeCRLF(String str) {
        return str.replaceAll("\\r?\\n$", "");
    }

}
