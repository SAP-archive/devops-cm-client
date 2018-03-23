package sap.prd.cmintegration.cli;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class GetChangeTransportDescriptionTest extends CMTransportTestBase {

    @Test
    public void getChangeTransportDesciptionStraightForward() throws Exception {

        setMock(setupMock("L21K900026", "xOwner", "xDescription", false));
        GetTransportDescription.main(new String[] {
                "-u", SERVICE_USER,
                "-p", SERVICE_PASSWORD,
                "-e", SERVICE_ENDPOINT,
                "dummy-cmd",
                "-cID", "8000038673", "-tID", "L21K900026"});

        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")),
                is(equalTo("xDescription")));

        assertThat(changeId.getValue(), is(equalTo("8000038673")));
    }
}
