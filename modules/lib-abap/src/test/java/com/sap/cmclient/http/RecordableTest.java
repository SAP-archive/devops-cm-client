package com.sap.cmclient.http;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Strings;

public class RecordableTest {

    private final static String USER = "aliBaba", PASSWORD = "openSesame";

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

    @After
    public void tearDown() {

        if(! isRecording()) {

            for(ServeEvent e : wireMockRule.getAllServeEvents())
                if(e.isNoExactMatch()) throw new RuntimeException("There was an unmatched request: " + e.getRequest().getAbsoluteUrl());

            WireMock.resetAllRequests();
            WireMock.resetAllScenarios();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if(isRecording()) {
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
        return isRecording() ? System.getProperty("THE_USER", USER) : USER;
    }
    
    protected static String getPassword() {
        return isRecording() ? System.getProperty("THE_PASSWORD", PASSWORD) : PASSWORD;
    }

    protected static boolean isRecording() {
        return getHost() != null && ! getHost().isEmpty();
    }
}
