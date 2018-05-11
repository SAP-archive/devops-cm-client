package sap.prd.cmintegration.cli;

import java.lang.reflect.Field;

import org.junit.After;

public class CMSolmanTestBase extends CMTestBase {

    @After
    public void tearDown() throws Exception {
        System.setOut(oldOut);
        setMock(null);
    }

    protected static void setMock(SolmanClientFactory mock) throws Exception {
        Field field = SolmanClientFactory.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, mock);
        field.setAccessible(false);
    }

}
