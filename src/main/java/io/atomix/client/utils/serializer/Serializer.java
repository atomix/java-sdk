// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.serializer;

/**
 * Interface for serialization of store artifacts.
 */
public interface Serializer {

    /**
     * Serialize the specified object.
     *
     * @param object object to serialize.
     * @param <T>    encoded type
     * @return serialized bytes.
     */
    <T> byte[] encode(T object);

    /**
     * Deserialize the specified bytes.
     *
     * @param bytes byte array to deserialize.
     * @param <T>   decoded type
     * @return deserialized object.
     */
    <T> T decode(byte[] bytes);

}
