package com.sap.cmclient.http;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Strings;

public class RecordableTest {

    private final static Path WIREMOCK_HOME = Paths.get("src/test/wiremock");

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(

            // "/mappings" is appended automagically
            wireMockConfig().dynamicPort().usingFilesUnderDirectory(WIREMOCK_HOME.toFile().getPath()));
    
    @BeforeClass
    public static void setupClass() throws URISyntaxException, IOException {

        if(!Strings.isNullOrEmpty(getHost())) {
            System.out.println("[INFO] Recording ...");
            wireMockRule.startRecording(getHost());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if(!Strings.isNullOrEmpty(getHost())) {
            wireMockRule.stopRecording();
            System.out.println("[INFO] ... recording done.");
        }
    }

    protected static String getWiremockProxy() {
        return String.format("http://localhost:%s", wireMockRule.port());
    }

    protected static String getHost() {
        return System.getProperty("THE_HOST");
    }

    protected static String getUser() {
        return System.getProperty("THE_USER", "aliBaba");
    }
    
    protected static String getPassword() {
        return System.getProperty("THE_PASSWORD", "openSesame");
    }
}
