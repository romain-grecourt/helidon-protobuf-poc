package io.helidon.protobuf.example;

/**
 *
 * @author rgrecour
 */
public class GreetingService extends AbstractGreetingService {

    private static String greeting = "Ciao";

    @Override
    public GreetingProtos.Greeting getDefaultGreeting(GreetingProtos.Void message) {
        return GreetingProtos.Greeting.newBuilder()
                .setGreeting(String.format("%s %s!", greeting, "World"))
                .build();
    }

    @Override
    public GreetingProtos.Greeting getGreeting(GreetingProtos.Name message){
        return GreetingProtos.Greeting.newBuilder()
                .setGreeting(String.format("%s %s!", greeting, message.getName()))
                .build();
    }

    @Override
    public GreetingProtos.Greeting setDefaultGreeting(GreetingProtos.Greeting message){
        greeting = message.getGreeting();
        return message;
    }
}
