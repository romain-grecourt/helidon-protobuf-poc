package io.helidon.protobuf.codegen;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import java.io.IOException;

/**
 *
 * @author rgrecour
 */
public class Main {

    public static void main(String[] args) throws IOException {
        CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in);
        CodeGeneratorResponse.Builder responseBuilder = CodeGeneratorResponse.newBuilder();
        for (FileDescriptorProto fileDescProto : request.getProtoFileList()) {
            FileOptions options = fileDescProto.getOptions();
            String packagePath = options.getJavaPackage().replace('.', '/') + "/";
            for (ServiceDescriptorProto serviceDescProto : fileDescProto.getServiceList()) {
                String serviceName = serviceDescProto.getName();
                StringBuilder content = new StringBuilder();
                // TODO
                // generate an abstract class with proper prototypes
                content.append("interface ")
                        .append(serviceName)
                        .append(" { }");
                responseBuilder.addFile(CodeGeneratorResponse.File.newBuilder()
                        .setName(packagePath + serviceName + ".java")
                        .setContent(content.toString()));
            }
        }
        CodeGeneratorResponse response = responseBuilder.build();
        response.writeTo(System.out);
    }
}
