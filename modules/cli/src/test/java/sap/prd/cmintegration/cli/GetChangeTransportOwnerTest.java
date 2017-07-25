package sap.prd.cmintegration.cli;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class GetChangeTransportOwnerTest extends CMTransportTestBase {

    @Test
    public void getChangeTransportOwnerStraightForward() throws Exception {

        setMock(setupMock("L21K900026", "xOwner", "xDesc", false));
        GetTransportOwner.main(new String[] {
                "-u", "john.doe",
                "-p", "openSesame",
                "-e", "https://example.org/endpoint/",
                "dummy-cmd",
                "8000038673", "L21K900026"});

        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")),
                is(equalTo("xOwner")));

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }
}
