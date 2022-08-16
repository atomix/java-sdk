// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.serializer;

public interface Serializer<T> {
    byte[] serialize(T object);
    T deserialize(byte[] bytes);
}
