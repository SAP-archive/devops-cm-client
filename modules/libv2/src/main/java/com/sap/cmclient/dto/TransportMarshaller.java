package com.sap.cmclient.dto;

import java.util.HashMap;
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
    
    
    public static Map<String, Object> put(Transport t)
    {
      Map<String, Object> p = new HashMap<String, Object>();
      
      p.put("Id", t.getId());
      p.put("Owner", t.getOwner());
      p.put("Description", t.getDescription());
      p.put("TarSystem", t.getTargetSystem());
      p.put("Status", t.getStatus().toString());
      p.put("Type", t.getType().toString());
      return p;
    }
    
    private static String asString(Map<String, ?> m, String key) {
        return (String)m.get(key);
    }
}
