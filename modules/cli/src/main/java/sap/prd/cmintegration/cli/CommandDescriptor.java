package sap.prd.cmintegration.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sap.prd.cmintegration.cli.TransportRetriever.BackendType;

/**
 * Contains the name of the command as it is used from the command line.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface CommandDescriptor {
    String name();
    BackendType type();
}
