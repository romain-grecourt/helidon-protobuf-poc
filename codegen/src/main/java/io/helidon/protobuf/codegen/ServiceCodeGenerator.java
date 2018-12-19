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

package io.helidon.protobuf.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.protobuf.DescriptorProtos;

import static io.helidon.protobuf.codegen.HttpEndpoint.HttpVerb.*;

/**
 *
 * @author rgrecour
 */
public class ServiceCodeGenerator {

    public static class ServiceCode {

        private final String content;
        private final String filePath;

        private ServiceCode(String content, String filePath) {
            this.content = content;
            this.filePath = filePath;
        }

        public String content(){
            return content;
        }

        public String filePath(){
            return filePath;
        }
    }

    public static ServiceCode generate(final DescriptorProtos.ServiceDescriptorProto service,
                                       final DescriptorProtos.FileOptions options) {

        final List<HttpEndpoint> httpEndpoints = HttpEndpoint.create(
                service.getMethodList(), options);

        final String serviceName = Character.toUpperCase(service.getName().charAt(0))
                + service.getName().substring(1);

        final StringBuilder content = new StringBuilder();

        // package declaration
        content.append("package ")
                .append(options.getJavaPackage())
                .append(";\n\n");

        // imports
        final Set<String> packages = new HashSet<>();
        packages.add("io.helidon.webserver.Routing");
        packages.add("io.helidon.webserver.ServerRequest");
        packages.add("io.helidon.webserver.ServerResponse");
        packages.add("io.helidon.webserver.Service");
        packages.add("io.helidon.protobuf.support.ProtobufSupport");
        final List<String> sortedPackages = new ArrayList<String>(packages);
        Collections.sort(sortedPackages);
        for (String pkg : sortedPackages) {
            content.append("import ")
                    .append(pkg)
                    .append(";\n");
        }

        // class declaration start
        content.append("\npublic abstract class Abstract")
                .append(serviceName)
                .append(" implements Service {")
                .append("\n\n");

        // update rules start
        content.append("    @Override\n")
                .append("    public void update(Routing.Rules rules) {")
                .append("\n        rules")
                .append("\n              // register writer for all output messages")
                .append("\n             .register(ProtobufSupport.builder().build())");
        for (HttpEndpoint httpEndpoint : httpEndpoints) {
            if (httpEndpoint.verb() == HttpEndpoint.HttpVerb.POST
                    || httpEndpoint.verb() == HttpEndpoint.HttpVerb.POST) {
                content.append("\n              // register reader for input message")
                        .append("\n             .")
                        .append(httpEndpoint.verb().toString().toLowerCase())
                        .append("(\"")
                        .append(httpEndpoint.path())
                        .append("\", ProtobufSupport.create(")
                        .append(httpEndpoint.inputType())
                        .append(".getDefaultInstance()))");
            }
            content.append("\n             .")
                    .append(httpEndpoint.verb().toString().toLowerCase())
                    .append("(\"")
                    .append(httpEndpoint.path())
                    .append("\", this::")
                    .append(httpEndpoint.name())
                    .append("Handler)");
        }
        if (!httpEndpoints.isEmpty()) {
            content.append(';');
        }
        // update rules end
        content.append("\n    }\n");

        // abstract methods declaration
        for (HttpEndpoint httpEndpoint : httpEndpoints) {
            content.append("\n    public abstract ")
                    .append(httpEndpoint.outputType())
                    .append(" ")
                    .append(httpEndpoint.name())
                    .append("(")
                    .append(httpEndpoint.inputType())
                    .append(" message);");
        }
        content.append('\n');

        // handler methods
        for (HttpEndpoint httpEndpoint : httpEndpoints) {
            content.append("\n    private void ")
                    .append(httpEndpoint.name())
                    .append("Handler(final ServerRequest req, final ServerResponse res) {\n");
            switch (httpEndpoint.verb()) {
                case ANY:
                case GET:
                case DELETE:
                case PATCH:
                    content.append("        ")
                            .append(httpEndpoint.inputType())
                            .append(".Builder inputMessageBuilder = ")
                            .append(httpEndpoint.inputType())
                            .append(".newBuilder();\n");
                    // path params
                    for (String pathParam : httpEndpoint.pathParams()) {
                        content.append("        inputMessageBuilder.set")
                                .append(Character.toUpperCase(pathParam.charAt(0)))
                                .append(pathParam.substring(1))
                                .append("(req.path().param(\"")
                                .append(pathParam)
                                .append("\"));\n");
                    }
                    content.append("        ")
                            .append(httpEndpoint.inputType())
                            .append(" inputMessage = inputMessageBuilder.build();\n")
                            .append("        res.send(")
                            .append(httpEndpoint.name())
                            .append("(inputMessage));");
                    break;
                case POST:
                case PUT:
                    content.append("        req.content().as(")
                            .append(httpEndpoint.inputType())
                            .append(".class).thenAccept((inputMessage) -> {\n")
                            .append("           res.status(201).send(")
                            .append(httpEndpoint.name())
                            .append("(inputMessage));\n")
                            .append("        });");
                    break;
            }
            content.append("\n    }\n");
        }

        // class declaration end
        content.append("}\n");

        final String filePath = options.getJavaPackage().replace('.', '/')
                + "/Abstract" + serviceName + ".java";

        return new ServiceCode(content.toString(), filePath);
    }
}
