package com.sap.cmclient.dto;

import java.util.GregorianCalendar;

public class Transport {

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

    
    private  String id,
                         owner, 
                         description,
                         requestRef,
                         cloud,
                         targetSystem;
    
    private final GregorianCalendar date, time;

    private Status status;
    private final Type type;

    public Transport(String id,
                     String owner,
                     String description,
                     String targetSystem,
                     GregorianCalendar date,
                     GregorianCalendar time,
                     String requestRef,
                     String cloud,
                     Status status,
                     Type type) {
        this.id = id;
        this.owner = owner;
        this.description = description;
        this.targetSystem = targetSystem;
        this.date = date;
        this.time = time;
        this.requestRef = requestRef;
        this.cloud = cloud;
        this.status = status;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getDescription() {
        return description;
    }

    public String getTargetSystem() {
        return targetSystem;
    }

    public Status getStatus() {
        return status;
    }
    
    public Type getType() {
        return type;
    }
    
    public void setDescription(String d)
    {
      this.description = d;
    }
    
    public GregorianCalendar getDate()
    {
      return date;
    }

    public GregorianCalendar getTime()
    {
      return time;
    }

    public String getRequestRef()
    {
      return requestRef;
    }

    public String getCloud()
    {
      return cloud;
    }
    
    public void setId(String id)
    {
      this.id = id;
    }

    public void setOwner(String owner)
    {
      this.owner = owner;
    }

    public void setRequestRef(String requestRef)
    {
      this.requestRef = requestRef;
    }

    public void setCloud(String cloud)
    {
      this.cloud = cloud;
    }

    public void setTargetSystem(String targetSystem)
    {
      this.targetSystem = targetSystem;
    }
    
    public void setStatus(Status s)
    {
      this.status = s;
    }

    @Override
    public String toString() {
        return "Transport [id=" + id + ", owner=" + owner + ", description=" + description + ", targetSystem="
                + targetSystem + ", date= " + date +", time= " + time + ", requestRef= "+ requestRef + ", cloud= " + cloud +", status=" + status + ", type=" + type + "]";
    }
}
