package io.helidon.protobuf.codegen;

import io.helidon.protobuf.support.ProtobufSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public abstract class AbstractGreetingService implements Service {

    @Override
    public void update(Routing.Rules rules) {
        rules
              // register writer for all output messages
             .register(ProtobufSupport.builder().build())
             .get("/", this::getDefaultGreetingHandler)
             .get("/{name}", this::getGreetingHandler)
              // register reader for input message
             .post("/", ProtobufSupport.create(GreetingProtos.Greeting.getDefaultInstance()))
             .post("/", this::setDefaultGreetingHandler);
    }

    public abstract GreetingProtos.Greeting getDefaultGreeting(GreetingProtos.Void message);
    public abstract GreetingProtos.Greeting getGreeting(GreetingProtos.Name message);
    public abstract GreetingProtos.Greeting setDefaultGreeting(GreetingProtos.Greeting message);

    private void getDefaultGreetingHandler(final ServerRequest req, final ServerResponse res) {
        GreetingProtos.Void.Builder inputMessageBuilder = GreetingProtos.Void.newBuilder();
        GreetingProtos.Void inputMessage = inputMessageBuilder.build();
        res.send(getDefaultGreeting(inputMessage));
    }

    private void getGreetingHandler(final ServerRequest req, final ServerResponse res) {
        GreetingProtos.Name.Builder inputMessageBuilder = GreetingProtos.Name.newBuilder();
        inputMessageBuilder.setName(req.path().param("name"));
        GreetingProtos.Name inputMessage = inputMessageBuilder.build();
        res.send(getGreeting(inputMessage));
    }

    private void setDefaultGreetingHandler(final ServerRequest req, final ServerResponse res) {
        req.content().as(GreetingProtos.Greeting.class).thenAccept((inputMessage) -> {
           res.status(201).send(setDefaultGreeting(inputMessage));
        });
    }
}
