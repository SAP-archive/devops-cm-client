package sap.prd.cmintegration.cli;

import org.junit.After;

public class ABAPBackendTest extends CMABAPBackendTestBase {

    @After
    public void tearDown() throws Exception {
        System.setOut(oldOut);
        setMock(null);
    }
}

