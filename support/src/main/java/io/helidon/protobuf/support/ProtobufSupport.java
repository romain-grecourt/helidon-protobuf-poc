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

package io.helidon.protobuf.support;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.helidon.common.http.DataChunk;
import io.helidon.common.http.MediaType;
import io.helidon.common.http.Reader;
import io.helidon.common.reactive.Flow;
import io.helidon.webserver.ContentReaders;
import io.helidon.webserver.ContentWriters;
import io.helidon.webserver.Handler;
import io.helidon.webserver.RequestHeaders;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

import static io.helidon.common.CollectionsHelper.mapOf;

/**
 *
 * @author rgrecour
 */
public class ProtobufSupport implements Service, Handler {

    private static final MediaType X_PROTOBUF = new MediaType("application", "x-protobuf");
    private static final MediaType X_PROTOBUFFER = new MediaType("application", "x-protobuffer");

    private final MessageLite inputPrototype;
    private final FileDescriptor fileDescriptor;
    private final ExtensionRegistryLite extensionRegistry;

    private ProtobufSupport(MessageLite prototype, FileDescriptor fileDescriptor,
            ExtensionRegistryLite extensionRegistry) {
        this.inputPrototype = prototype;
        this.fileDescriptor = fileDescriptor;
        this.extensionRegistry = extensionRegistry;
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.any(this);
    }

    @Override
    public void accept(ServerRequest req, ServerResponse res) {
        req.content().registerReader(MessageLite.class::isAssignableFrom,
                (publisher, type) -> {
                    Parser<? extends MessageLite> messageParser =
                            getMessageParser(req.headers());
                    if(messageParser == null){
                        throw new IllegalStateException("Message type unkown");
                    }
                    return reader(messageParser).apply(publisher);
                });
        res.registerWriter(message -> (message instanceof Message)
                && testOrSetContentType(req, res, (Message) message), (message) -> {
            return writer().apply((Message) message);
        });
        req.next();
    }

    private boolean testOrSetContentType(ServerRequest req, ServerResponse res,
            Message message) {

        MediaType mt = res.headers().contentType().orElse(null);
        if (mt == null) {
            List<MediaType> acceptedTypes = req.headers().acceptedTypes();
            MediaType preferredType = acceptedTypes
                    .stream()
                    .map(type -> {
                        if (type.test(X_PROTOBUF)) {
                            return X_PROTOBUF;
                        }
                        if (type.test(X_PROTOBUFFER)) {
                            return X_PROTOBUFFER;
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (preferredType == null) {
                return false;
            } else {
                String messageType = message.getDescriptorForType().getFullName();
                MediaType contentType = new MediaType(
                        preferredType.getType(),
                        preferredType.getSubtype(),
                        mapOf("messageType", messageType));
                res.headers().contentType(contentType);
                return true;
            }
        } else {
            return X_PROTOBUF.test(mt) || X_PROTOBUFFER.test(mt);
        }
    }

    private Parser<? extends MessageLite> getMessageParser(RequestHeaders headers) {
        if(inputPrototype != null){
            return inputPrototype.getParserForType();
        }
        if(fileDescriptor == null){
            return null;
        }
        if (headers.contentType().isEmpty()) {
            return null;
        }
        MediaType mt = headers.contentType().get();
        if (!"application".equals(mt.getType())) {
            return null;
        }
        if(!(mt.test(X_PROTOBUF) || mt.test(X_PROTOBUF))){
            return null;
        }
        String messageType = mt.getParameters().get("messageType");
        if(messageType == null){
            return null;
        }
        Descriptor desc = fileDescriptor.findMessageTypeByName(messageType);
        return desc.toProto().getParserForType();
    }

    private Reader<MessageLite> reader(Parser<? extends MessageLite> parser) {
        return (publisher, clazz)
                -> ContentReaders.byteArrayReader()
                        .apply(publisher)
                        .thenApply(bytes -> {
                            try {
                                if (extensionRegistry == null) {
                                    return parser.parseFrom(bytes);
                                } else {
                                    return parser.parseFrom(bytes, extensionRegistry);
                                }
                            } catch (InvalidProtocolBufferException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
    }

    private Function<MessageLite, Flow.Publisher<DataChunk>> writer() {
        return message -> {
            return ContentWriters.byteArrayWriter(false).apply(message.toByteArray());
        };
    }

    public static ProtobufSupport create(MessageLite prototype){
        return builder()
                .input(prototype)
                .build();
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder implements io.helidon.common.Builder<ProtobufSupport> {

        private MessageLite prototype = null;
        private FileDescriptor fileDescriptor = null;
        private ExtensionRegistryLite extensionRegistry = null;

        public Builder extensionRegistry(ExtensionRegistryLite ext){
            this.extensionRegistry = ext;
            return this;
        }

        public Builder input(MessageLite prototype){
            this.prototype = prototype;
            return this;
        }

        public Builder descriptor(FileDescriptor desc) {
            this.fileDescriptor = desc;
            return this;
        }

        @Override
        public ProtobufSupport build() {
            return new ProtobufSupport(prototype, fileDescriptor,
                    extensionRegistry);
        }
    }
}
