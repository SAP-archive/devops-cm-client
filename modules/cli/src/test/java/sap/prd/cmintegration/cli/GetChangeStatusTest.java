package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.*;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Test;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class GetChangeStatusTest {

    @Test
    public void testGetChangeStatusStraightForward() throws Exception {

        CMODataChange changeMock = EasyMock.createMock(CMODataChange.class);
        expect(changeMock.getStatus()).andReturn("E0002");

        CMODataClient clientMock = EasyMock.createMock(CMODataClient.class);
        expect(clientMock.getChange("8000038673")).andReturn(changeMock);

        EasyMock.replay(changeMock, clientMock);

        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(byteOS), oldOut = System.out;
        System.setOut(out);

        //
        // Comment line below in order to go against the real back-end as specified via -h
        GetChangeStatus.client = clientMock;

        try {
          GetChangeStatus.main(new String[] {
          "-c", "8000038673",
          "-u", "john.doe",
          "-p", "openSesame",
          "-h", "https://example.org/endpoint/"});
        } finally {
            IOUtils.closeQuietly(out);
            System.setOut(oldOut);
        }

        assertThat(GetChangeStatus.getChangeId(), is(equalTo("8000038673")));
        assertThat(GetChangeStatus.getUser(), is(equalTo("john.doe")));
        assertThat(GetChangeStatus.getPassword(), is(equalTo("openSesame")));
        assertThat(GetChangeStatus.getHost(), is(equalTo("https://example.org/endpoint/")));

        assertThat(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(byteOS.toByteArray()), "UTF-8")).readLine(), equalTo("E0002"));
    }
}
