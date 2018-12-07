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
