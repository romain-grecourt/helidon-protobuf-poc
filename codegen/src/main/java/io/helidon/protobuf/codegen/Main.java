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
