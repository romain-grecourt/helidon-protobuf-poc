package io.helidon.protobuf.example;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.helidon.webserver.WebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author rgrecour
 */
public class GreetingServiceTest {

    private static WebServer webServer;
    private static String greetingPrefix = "Ciao";

    @BeforeAll
    public static void startTheServer() throws Exception {
        webServer = Main.startServer();
        while (!webServer.isRunning()) {
            Thread.sleep(1 * 1000);
        }
    }

    @AfterAll
    public static void stopServer() throws Exception {
        if (webServer != null) {
            webServer.shutdown()
                    .toCompletableFuture()
                    .get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testGetDefaultGreeting() throws IOException {
        System.out.println("running testGetDefaultGreeting");
        HttpURLConnection conn = getURLConnection("GET", "/greet");
        conn.setDoInput(true);
        GreetingProtos.Greeting greeting = GreetingProtos.Greeting
                .parseFrom(conn.getInputStream());
        assertEquals(greetingPrefix + " World!", greeting.getGreeting());
    }

    @Test
    public void testGetGreeting() throws IOException {
        System.out.println("running testGetGreeting");
        HttpURLConnection conn = getURLConnection("GET", "/greet/Romano");
        conn.setDoInput(true);
        GreetingProtos.Greeting greeting = GreetingProtos.Greeting
                .parseFrom(conn.getInputStream());
        assertEquals(greetingPrefix + " Romano!", greeting.getGreeting());
    }

    @Test
    public void setDefaultGreeting() throws IOException {
        System.out.println("running setDefaultGreeting");
        HttpURLConnection conn = getURLConnection("POST", "/greet");
        GreetingProtos.Greeting input = GreetingProtos.Greeting.newBuilder()
                .setGreeting("Hello")
                .build();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        input.writeTo(conn.getOutputStream());
        GreetingProtos.Greeting output = GreetingProtos.Greeting
                .parseFrom(conn.getInputStream());
        assertEquals(input.getGreeting(), output.getGreeting());
        greetingPrefix = "Hello";
    }

    private HttpURLConnection getURLConnection(String method, String path) throws IOException {
        URL url = new URL("http://localhost:" + webServer.port() + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/x-protobuf");
        return conn;
    }
}
