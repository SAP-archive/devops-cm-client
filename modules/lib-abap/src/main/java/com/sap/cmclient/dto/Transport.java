package com.sap.cmclient.dto;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.odata2.api.ep.entry.ODataEntry;

public class Transport {

  private static final String ID = "Id";
  private static final String OWNER = "Owner";
  private static final String DESCRIPTION = "Description";
  private static final String TARGETSYSTEM = "TarSystem";
  private static final String DATE = "Date";
  private static final String TIME = "Time";
  private static final String REQUESTREF = "RequestRef";
  private static final String CLOUD = "Cloud";
  private static final String STATUS = "Status";
  private static final String TYPE = "Type";
  
  
  public enum Status {
                          D ("development"),
                          R ("release"),
                          UNKNOWN("unknown"); // in order to simplify null handling.

                          String description;

                          Status(String description) {
                              this.description = description;
                          }

                          public String getDescription() {
                              return description;
                          }

                          public static Status get(String name) {
                              for(Status value : values()) {
                                  if(value.name().equals(name))
                                      return value;
                              }
                              return Status.UNKNOWN;
                          }
                       }

    public enum Type {
        W ("???"), // TODO clarify semantic
        K ("???"),
        UNKNOWN("unknown"); // in order to simplify null handling.

        String description;

        Type(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static Type get(String name) {
            for(Type value : values()) {
                if(value.name().equals(name))
                    return value;
            }
            return Type.UNKNOWN;
        }
     }

    
    private final Map<String, Object> values;
    
    public Transport(ODataEntry entry)
    {
      if(entry.getProperties().get(ID) == null) throw new IllegalArgumentException("Key Id must not be null.");
      values =  new HashMap<String, Object>(entry.getProperties());
    }
     
    private Transport()
    {
      values = null;
    }
    
    public static Map<String, Object> getTransportCreationRequestMap( String owner, 
                           String description, 
                           String targetSystem,
                           String requestRef,
                           Type type )
    {
      Map<String, Object> m = new HashMap<String, Object>();
      GregorianCalendar cal = new GregorianCalendar();
      GregorianCalendar time = new GregorianCalendar(0, 0, 0, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
      m.put(OWNER, owner);
      m.put(DESCRIPTION, description);
      m.put(TARGETSYSTEM, targetSystem);
      m.put(REQUESTREF, requestRef);
      m.put(TYPE, type.toString());
      m.put(ID, "");
      m.put(STATUS, "");
      m.put(DATE, cal);
      m.put(TIME, time);
      m.put(CLOUD, "X");
      return m;
      
    }
    
    
    public String getId() {
        return (String) values.get(ID);
    }

    public String getOwner() {
        return (String) values.get(OWNER);
    }
    
    public void setOwner(String owner) {
        values.put(OWNER, owner);
    }

    public String getDescription() {
        return (String) values.get(DESCRIPTION);
    }
    
    public void setDescription(String description) {
        values.put(DESCRIPTION, description);   
    }

    public String getTargetSystem() {
        return (String) values.get(TARGETSYSTEM);
    }

    public Status getStatus() {
        return Status.get((String) values.get(STATUS));
    }
    
    public void setStatus(Status status) {
        values.put(STATUS, status.toString());
    }
    
    public Type getType() {
        return Type.get((String) values.get(TYPE));
    }
     
    public GregorianCalendar getDate()
    {
      return (GregorianCalendar) values.get(DATE);
    }

    public GregorianCalendar getTime()
    {
      return (GregorianCalendar) values.get(TIME);
    }

    public String getRequestRef()
    {
      return (String) values.get(REQUESTREF);
    }

    public String getCloud()
    {
      return (String) values.get(CLOUD);
    }
    
    public Map<String, Object> getValueMap(){
      return new HashMap<String, Object>(values);
    }
    
    @Override
    public String toString() {
        return "Transport [id=" + getId() + ", owner=" + getOwner() + ", description=" + getDescription() + ", targetSystem="
                + getTargetSystem() + ", date= " + getDate() +", time= " + getTime() + ", requestRef= "+ getRequestRef() + ", cloud= " + getCloud() +", status=" + getStatus() + ", type=" + getType() + "]";
    }
    
    @Override
    public int hashCode() {
      return values.get(ID).hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
      if(this != o) return false;
      if (o instanceof Transport) {
      Transport t = (Transport) o;
      return this.values.equals(t.values);
      }
      else {
        return false;
      }
    }
}
