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
        packages.add("com.google.protobuf.InvalidProtocolBufferException");
        packages.add("com.google.protobuf.util.JsonFormat");
        final List<String> sortedPackages = new ArrayList<String>(packages);
        Collections.sort(sortedPackages);
        for(String pkg : sortedPackages){
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
                .append("    public void update(Routing.Rules rules) {");
        boolean firstRule = true;
        for(HttpEndpoint httpEndpoint : httpEndpoints){
            if(firstRule){
                content.append("\n        rules.");
            } else {
                content.append("\n             .");
            }
            content.append(httpEndpoint.verb().toString().toLowerCase())
                   .append("(\"")
                   .append(httpEndpoint.path())
                   .append("\", this::")
                   .append(httpEndpoint.name())
                   .append("Handler)");
            if(firstRule){
                firstRule = false;
            }
        }
        if(!httpEndpoints.isEmpty()){
            content.append(';');
        }
        // update rules end
        content.append("\n    }\n");

        // abstract methods declaration
        for(HttpEndpoint httpEndpoint : httpEndpoints){
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
        for(HttpEndpoint httpEndpoint : httpEndpoints){
            content.append("\n    private void ")
                        .append(httpEndpoint.name())
                        .append("Handler(final ServerRequest req, final ServerResponse res) {\n");
            switch(httpEndpoint.verb()){
                case ANY:
                case GET:
                case DELETE:
                case PATCH:
                    content.append("        try {\n")
                            .append("            ")
                            .append(httpEndpoint.inputType())
                            .append(".Builder inputMessageBuilder = ")
                            .append(httpEndpoint.inputType())
                            .append(".newBuilder();\n");
                    // path params
                    for(String pathParam : httpEndpoint.pathParams()){
                        content.append("            inputMessageBuilder.set")
                                .append(Character.toUpperCase(pathParam.charAt(0)))
                                .append(pathParam.substring(1))
                                .append("(req.path().param(\"")
                                .append(pathParam)
                                .append("\"));\n");
                    }
                    content.append("            ")
                            .append(httpEndpoint.inputType())
                            .append(" inputMessage = inputMessageBuilder.build();\n")
                            .append("            res.send(JsonFormat.printer().print(")
                            .append(httpEndpoint.name())
                            .append("(inputMessage)));\n")
                            .append("        } catch (InvalidProtocolBufferException ex) {\n")
                            .append("            req.next(ex);\n")
                            .append("        }\n");
                break;
                case POST:
                case PUT:
                    content.append("        req.content().as(String.class).thenAccept((json) -> {\n")
                            .append("            try {\n")
                            .append("                ")
                            .append(httpEndpoint.inputType())
                            .append(".Builder inputMessageBuilder = ")
                            .append(httpEndpoint.inputType())
                            .append(".newBuilder();\n")
                            .append("                JsonFormat.parser().merge(json, inputMessageBuilder);\n")
                            .append("                ")
                            .append(httpEndpoint.inputType())
                            .append(" inputMessage = inputMessageBuilder.build();\n")
                            .append("                res.status(201).send(JsonFormat.printer().print(")
                            .append(httpEndpoint.name())
                            .append("(inputMessage)));\n")
                            .append("            } catch (InvalidProtocolBufferException ex) {\n")
                            .append("                req.next(ex);\n")
                            .append("            }\n")
                            .append("        });\n");
                            
                    break;
            }
            content.append("    }\n");
        }

        // class declaration end
        content.append("}\n");

        final String filePath = options.getJavaPackage().replace('.', '/')
                + "/Abstract" + serviceName + ".java";

        return new ServiceCode(content.toString(), filePath);
    }
}
