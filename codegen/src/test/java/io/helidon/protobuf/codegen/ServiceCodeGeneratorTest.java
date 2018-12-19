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

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import java.util.List;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author rgrecour
 */
public class ServiceCodeGeneratorTest {

    @Test
    public void testGreetingService(){
        // trigger code generation
        CodeGeneratorRequest.Builder requestBuilder = CodeGeneratorRequest.newBuilder();
        requestBuilder.addFileToGenerate("greeting.proto");
        requestBuilder.addProtoFile(GreetingProtos.getDescriptor().toProto());
        CodeGeneratorRequest request = requestBuilder.build();
        CodeGeneratorResponse.Builder responseBuilder = CodeGeneratorResponse.newBuilder();
        Main.generate(request, responseBuilder);
        List<CodeGeneratorResponse.File> fileList = responseBuilder.getFileList();

        // check file size
        assertEquals(1, fileList.size(), "file list size");
        CodeGeneratorResponse.File file = fileList.get(0);

        // check name (file path)
        assertEquals("io/helidon/protobuf/codegen/AbstractGreetingService.java", file.getName(), "file name");

        // write the generated file content for debugging purposes
        File actual = new File("target/AbstractGreetingService.java.txt");
        FileWriter writer;
        try {
            writer = new FileWriter(actual);
            writer.append(file.getContent());
            writer.close();
        } catch (IOException ex) {
            fail("error while writing actual", ex);
        }

        try {
            // diff expected and actual
            File expected = new File("src/test/java/io/helidon/protobuf/codegen/AbstractGreetingService.java");

            List<String> expectedLines = Files.readAllLines(expected.toPath());
            List<String> actualLines = Files.readAllLines(actual.toPath());

            // compare expected and actual
            Patch<String> patch = DiffUtils.diff(expectedLines, actualLines);
            if (patch.getDeltas().size() > 0) {
                fail("rendered file differs from expected: " + patch.toString());
            }
        } catch (IOException | DiffException ex) {
            fail("error while diffing actual and expected", ex);
        }
    }
}
