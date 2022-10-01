// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.utils.serializer;

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
