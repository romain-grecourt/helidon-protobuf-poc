/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
