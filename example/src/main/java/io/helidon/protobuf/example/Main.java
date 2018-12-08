package io.helidon.protobuf.example;

import java.io.IOException;
import java.net.InetAddress;

import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;

public final class Main {

    private Main() { }

    private static Routing createRouting() {
        return Routing.builder()
                .register("/greet", new GreetingService())
                .build();
    }

    public static void main(final String[] args) throws IOException {
        startServer();
    }

    protected static WebServer startServer() throws IOException {

        ServerConfiguration serverConfig = ServerConfiguration.builder()
                        .bindAddress(InetAddress.getByName("0.0.0.0"))
                        .port(8080)
                        .build();

        WebServer server = WebServer.create(serverConfig, createRouting());

        server.start().thenAccept(ws -> {
            System.out.println(
                    "WEB server is up! http://localhost:" + ws.port());
        });

        server.whenShutdown().thenRun(()
                -> System.out.println("WEB server is DOWN. Good bye!"));

        return server;
    }
}
