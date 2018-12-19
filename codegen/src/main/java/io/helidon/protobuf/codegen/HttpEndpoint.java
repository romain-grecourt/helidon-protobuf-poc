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

import com.google.api.AnnotationsProto;
import com.google.api.HttpRule;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rgrecour
 */
public class HttpEndpoint {

    private static final Pattern PATTERN_REFERENCE = Pattern.compile("\\{([^}]+)\\}");
    private final String name;
    private final HttpRule rule;
    private final HttpVerb verb;
    private final String path;
    private final String inputType;
    private final String outputType;
    private final List<String> pathParams;

    public static enum HttpVerb {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        ANY
    }

    private HttpEndpoint(final String name,
            final HttpRule rule,
            final HttpVerb verb,
            final String path,
            final String inputType,
            final String outputType) {

        this.name = name;
        this.rule = rule;
        this.verb = verb;
        this.path = path;
        this.inputType = inputType == null ? "void" : inputType;
        this.outputType = outputType == null ? "void" : outputType;
        this.pathParams = new ArrayList<>();
        Matcher matcher = PATTERN_REFERENCE.matcher(path);
        while (matcher.find()) {
            final String group = matcher.group();
            if(group.length() > 2){
                this.pathParams.add(group.substring(1, group.length() - 1));
            }
        }
    }

    public String name() {
        return name;
    }

    public HttpRule rule() {
        return rule;
    }

    public HttpVerb verb() {
        return verb;
    }

    public String path() {
        return path;
    }

    public List<String> pathParams(){
        return pathParams;
    }

    public String inputType() {
        return inputType;
    }

    public String outputType() {
        return outputType;
    }

    public static List<HttpEndpoint> create(final List<MethodDescriptorProto> methodList,
            final FileOptions options) {

        final ArrayList<HttpEndpoint> endpoints = new ArrayList<>();
        for (MethodDescriptorProto method : methodList) {
            final HttpRule rule = getHttpAnnotation(method);
            if (rule == null) {
                continue;
            }

            HttpVerb verb;
            String path;
            if (isNonNullAndNonEmpty(rule.getGet())) {
                verb = HttpVerb.GET;
                path = rule.getGet();
            } else if (isNonNullAndNonEmpty(rule.getPost())) {
                verb = HttpVerb.POST;
                path = rule.getPost();
            } else if (isNonNullAndNonEmpty(rule.getPut())) {
                verb = HttpVerb.PUT;
                path = rule.getPut();
            } else if (isNonNullAndNonEmpty(rule.getDelete())) {
                verb = HttpVerb.DELETE;
                path = rule.getDelete();
            } else if (isNonNullAndNonEmpty(rule.getPatch())) {
                verb = HttpVerb.PATCH;
                path = rule.getPatch();
            } else {
                verb = HttpVerb.ANY;
                path = "/";
            }

            final String name = Character.toLowerCase(method.getName().charAt(0))
                    + method.getName().substring(1);
            final String inputType = options.getJavaOuterClassname()
                    + method.getInputType();
            final String outputType = options.getJavaOuterClassname()
                    + method.getOutputType();
            endpoints.add(new HttpEndpoint(name, rule, verb, path, inputType,
                    outputType));
        }
        return endpoints;
    }

    private static boolean isNonNullAndNonEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    private static HttpRule getHttpAnnotation(final MethodDescriptorProto method) {
        final Object httpAnnot = method.getOptions()
                .getExtension(AnnotationsProto.http);
        if (httpAnnot != null && (httpAnnot instanceof HttpRule)) {
            return (HttpRule) httpAnnot;
        }
        return null;
    }
}
