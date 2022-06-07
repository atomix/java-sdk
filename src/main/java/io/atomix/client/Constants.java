// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.grpc.Context;
import io.grpc.Metadata;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class Constants {
    /**
     * Context key carrying over the application id.
     */
    public static final Context.Key<String> APPLICATION_ID_CTX = Context.key("Application-ID");
    /**
     * Metadata key carrying over the application id.
     */
    public static final Metadata.Key<String> APPLICATION_ID_META = Metadata.Key.of("Application-ID", ASCII_STRING_MARSHALLER);
    /**
     * Context key carrying over the primitive id.
     */
    public static final Context.Key<String> PRIMITIVE_ID_CTX = Context.key("Primitive-ID");
    /**
     * Metadata key carrying over the primitive id.
     */
    public static final Metadata.Key<String> PRIMITIVE_ID_META = Metadata.Key.of("Primitive-ID", ASCII_STRING_MARSHALLER);
    /**
     * Context key carrying over the session id.
     */
    public static final Context.Key<String> SESSION_ID_CTX = Context.key("Session-ID");
    /**
     * Metadata key carrying over the session id.
     */
    public static final Metadata.Key<String> SESSION_ID_META = Metadata.Key.of("Session-ID", ASCII_STRING_MARSHALLER);
}
