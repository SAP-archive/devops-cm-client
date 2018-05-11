package com.sap.cmclient;

public interface Transport {

    String getTransportID();

    Boolean isModifiable();

    String getDescription();

    String getOwner();
}
