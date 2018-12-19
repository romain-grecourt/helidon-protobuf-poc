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
