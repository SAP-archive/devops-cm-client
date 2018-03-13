package com.sap.cmclient.dto;

import java.util.Map;

import org.apache.olingo.odata2.api.ep.entry.ODataEntry;

public class TransportMarshaller {

    public static Transport get(ODataEntry t) {
        Map<String, ?> p = t.getProperties();
        return new Transport(asString(p, "Id"), 
                             asString(p, "Owner"),
                             asString(p, "Description"),
                             asString(p, "TarSystem"),
                             Transport.Status.get(asString(p, "Status")),
                             Transport.Type.get(asString(p,"Type")));
    }
    
    private static String asString(Map<String, ?> m, String key) {
        return (String)m.get(key);
    }
}
