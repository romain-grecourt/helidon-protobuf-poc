package io.helidon.protobuf.codegen;

import java.io.IOException;

import com.google.api.AnnotationsProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import io.helidon.protobuf.codegen.ServiceCodeGenerator.ServiceCode;

/**
 *
 * @author rgrecour
 */
public class Main {

    public static void main(String[] args) throws IOException {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        registry.add(AnnotationsProto.http);
        CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in, registry);
        CodeGeneratorResponse.Builder responseBuilder = CodeGeneratorResponse.newBuilder();
        generate(request, responseBuilder);
        CodeGeneratorResponse response = responseBuilder.build();
        response.writeTo(System.out);
    }

    static void generate(final CodeGeneratorRequest request,
                         final CodeGeneratorResponse.Builder responseBuilder) {

        for (FileDescriptorProto protoFile : request.getProtoFileList()) {
            if(!request.getFileToGenerateList().contains(protoFile.getName())){
                continue;
            }
            for (ServiceDescriptorProto service : protoFile.getServiceList()) {
                ServiceCode serviceCode = ServiceCodeGenerator.generate(service, protoFile.getOptions());
                responseBuilder.addFile(CodeGeneratorResponse.File.newBuilder()
                        .setName(serviceCode.filePath())
                        .setContent(serviceCode.content()));
            }
        }
    }
}
