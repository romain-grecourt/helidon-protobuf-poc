package io.helidon.protobuf.codegen;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public abstract class AbstractGreetingService implements Service {

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/", this::getDefaultGreetingHandler)
             .get("/{name}", this::getGreetingHandler)
             .post("/", this::setDefaultGreetingHandler);
    }

    public abstract GreetingProtos.Greeting getDefaultGreeting(GreetingProtos.Void message);
    public abstract GreetingProtos.Greeting getGreeting(GreetingProtos.Name message);
    public abstract GreetingProtos.Greeting setDefaultGreeting(GreetingProtos.Greeting message);

    private void getDefaultGreetingHandler(final ServerRequest req, final ServerResponse res) {
        try {
            GreetingProtos.Void.Builder inputMessageBuilder = GreetingProtos.Void.newBuilder();
            GreetingProtos.Void inputMessage = inputMessageBuilder.build();
            res.send(JsonFormat.printer().print(getDefaultGreeting(inputMessage)));
        } catch (InvalidProtocolBufferException ex) {
            req.next(ex);
        }
    }

    private void getGreetingHandler(final ServerRequest req, final ServerResponse res) {
        try {
            GreetingProtos.Name.Builder inputMessageBuilder = GreetingProtos.Name.newBuilder();
            inputMessageBuilder.setName(req.path().param("name"));
            GreetingProtos.Name inputMessage = inputMessageBuilder.build();
            res.send(JsonFormat.printer().print(getGreeting(inputMessage)));
        } catch (InvalidProtocolBufferException ex) {
            req.next(ex);
        }
    }

    private void setDefaultGreetingHandler(final ServerRequest req, final ServerResponse res) {
        req.content().as(String.class).thenAccept((json) -> {
            try {
                GreetingProtos.Greeting.Builder inputMessageBuilder = GreetingProtos.Greeting.newBuilder();
                JsonFormat.parser().merge(json, inputMessageBuilder);
                GreetingProtos.Greeting inputMessage = inputMessageBuilder.build();
                res.status(201).send(JsonFormat.printer().print(setDefaultGreeting(inputMessage)));
            } catch (InvalidProtocolBufferException ex) {
                req.next(ex);
            }
        });
    }
}