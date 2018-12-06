package io.helidon.poc.protobuf;


import com.google.api.AnnotationsProto;
import com.google.api.HttpRule;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.helidon.webserver.Routing;
import io.helidon.webserver.Service;

/**
 *
 * @author rgrecour
 */
public class ProtobufSupport implements Service {

    private final FileDescriptor descriptor;

    public ProtobufSupport(FileDescriptor descriptor) {
        this.descriptor = descriptor;
        for(ServiceDescriptor serviceDesc : this.descriptor.getServices()){
            for(MethodDescriptor methodDesc : serviceDesc.getMethods()){
                Object httpAnnot = methodDesc.getOptions().getField(AnnotationsProto.http.getDescriptor());
                if(httpAnnot != null && (httpAnnot instanceof HttpRule)){
                    HttpRule httpRule = (HttpRule) httpAnnot;
                    System.out.println("rule.get: " + httpRule.getGet());
                }
            }
        }
    }

    @Override
    public void update(Routing.Rules rules) {
    }
}
