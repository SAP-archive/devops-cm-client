package com.sap.cmclient;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionHelper {

    private final static Logger logger = LoggerFactory.getLogger(VersionHelper.class);

    private VersionHelper() {
        // avoid getting instances.
    }

    public static String getLongVersion() {
        Properties vProps = getVersionProperties();
        return (vProps == null) ? "<n/a>" : format("%s : %s",
                                              vProps.getProperty("mvnProjectVersion", "<n/a>"),
                                              vProps.getProperty("gitCommitId", "<n/a>"));
    }

    public static String getShortVersion() {
        Properties vProps = getVersionProperties();
        return (vProps == null) ? "<n/a>" : vProps.getProperty("mvnProjectVersion", "<n/a>");
    }

    public static String getOlingoV2Version() {
        Properties vProps = getVersionProperties();
        return (vProps == null) ? "n/a" : vProps.getProperty("olingoVersionV2", "<n/a>");
    }

    public Object clone() {
        throw new UnsupportedOperationException();
    }

    private static Properties getVersionProperties() {
        try(InputStream version = VersionHelper.class.getResourceAsStream("/VERSION")) {
            Properties vProps = new Properties();
            vProps.load(version);
            return vProps;
        } catch(IOException e) {
            logger.warn("Cannot retrieve version.", e);
            return null;
        }
    }
}
