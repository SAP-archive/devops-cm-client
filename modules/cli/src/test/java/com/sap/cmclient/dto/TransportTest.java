package com.sap.cmclient.dto;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Maps;

public class TransportTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void testCreateTransportFromMap() {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        m.put("Description", "desc");
        m.put("TarSystem", "A5X");
        m.put("Type", "K");

        Transport t = new Transport(m);

        assertThat(t.getTransportID(), is(equalTo("999")));
        assertThat(t.getDescription(), is(equalTo("desc")));
        assertThat(t.getTargetSystem(), is(equalTo("A5X")));
        assertThat(t.getType(), is(equalTo("K")));
    }

    @Test
    public void testTransportWithoutIdRaisesIllegalArgumentException() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Key 'Id' must not be blank");

        // The map created below does not have an id member
        new Transport(Maps.newHashMap());
    }
}
