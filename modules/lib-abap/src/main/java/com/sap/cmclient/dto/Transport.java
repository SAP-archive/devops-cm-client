package com.sap.cmclient.dto;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;


public class Transport implements com.sap.cmclient.Transport {

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

    private final Map<String, Object> values;

    public Transport(Map<String, Object> props)
    {
      if(props.get(ID) != null && !(props.get(ID) instanceof String)) throw new IllegalArgumentException(format("%s property has invalid type.", ID));
      if( isNullOrEmpty((String)props.get(ID))) throw new IllegalArgumentException(format("Key '%s' must not be blank.", ID));
      values =  new HashMap<String, Object>(props);
    }

    public static Map<String, Object> getTransportCreationRequestMap( String owner, 
                           String description, 
                           String targetSystem,
                           String requestRef,
                           String type )
    {
      Map<String, Object> m = new HashMap<String, Object>();
      GregorianCalendar cal = new GregorianCalendar();
      GregorianCalendar time = new GregorianCalendar(0, 0, 0, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
      m.put(OWNER, owner);
      m.put(DESCRIPTION, description);
      m.put(TARGETSYSTEM, targetSystem);
      m.put(REQUESTREF, requestRef);
      m.put(TYPE, type);
      m.put(ID, "");
      m.put(STATUS, "");
      m.put(DATE, cal);
      m.put(TIME, time);
      m.put(CLOUD, "X");
      return m;

    }

    public String getTransportID() {
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

    public String getStatus() {
        return  (String)values.get(STATUS);
    }

    public void setStatus(String status) {
        values.put(STATUS, status);
    }

    public String getType() {
        return (String) values.get(TYPE);
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
        return "Transport [id=" + getTransportID() + ", owner=" + getOwner() + ", description=" + getDescription() + ", targetSystem="
                + getTargetSystem() + ", date= " + getDate() +", time= " + getTime() + ", requestRef= "+ getRequestRef() + ", cloud= " + getCloud() +", status=" + getStatus() + ", type=" + getType() + "]";
    }

    @Override
    public int hashCode() {
      return values.get(ID).hashCode();
    }

    public Boolean isModifiable() {
        return "D".equals(getStatus());
    }

    @Override
    public boolean equals(Object o) {
      if(this != o) { 
        if (o instanceof Transport) {
          return this.values.get(ID).equals(((Transport) o).values.get(ID));
        }
        else {
          return false;
        }
      }
      else return true;
    }
}
