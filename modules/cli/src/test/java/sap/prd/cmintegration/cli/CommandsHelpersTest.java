package sap.prd.cmintegration.cli;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class CommandsHelpersTest {

    @Test
    public void testPasswordIsHidden() {
        String[] args = Commands.Helpers.hidePassword(new String[] {
            "-e", "http://example.org",
            "-u", "me",
            "-p", "topSecret"});

        assertThat(StringUtils.join(args, " "), is(equalTo(
                "-e http://example.org -u me -p ********")));
    }

    @Test
    public void testEmptyPasswordOptionDoesNotFail() {
        String[] args = Commands.Helpers.hidePassword(new String[] {
            "-e", "http://example.org",
            "-u", "me",
            "-p"});

        assertThat(StringUtils.join(args, " "), is(equalTo(
                "-e http://example.org -u me -p")));
    }

    @Test
    public void testDashAsPasswordNotHidden() {

        //Password read from stdin in this case.

        String[] args = Commands.Helpers.hidePassword(new String[] {
                "-e", "http://example.org",
                "-u", "me",
                "-p", "-"});

            assertThat(StringUtils.join(args, " "), is(equalTo(
                    "-e http://example.org -u me -p -")));
    }

}
