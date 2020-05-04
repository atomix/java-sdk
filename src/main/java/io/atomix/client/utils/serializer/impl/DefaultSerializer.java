/*
 * Copyright 2020-present Open Networking Foundation
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

package io.atomix.client.utils.serializer.impl;

import io.atomix.client.utils.serializer.Serializer;

import java.io.*;

public class DefaultSerializer implements Serializer {

    /**
     * Default serializer constructor
     */
    public DefaultSerializer() {}


    /**
     * Serialize the specified object.
     *
     * @param object object to serialize.
     * @param <T>    encoded type
     * @return serialized bytes.
     */
    @Override
    public <T> byte[] encode(T object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(byteArrayOutputStream);
            out.writeObject(object);
            out.flush();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return byteArray;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * Deserialize the specified bytes.
     *
     * @param bytes byte array to deserialize.
     * @param <T>   decoded type
     * @return deserialized object.
     */
    @Override
    public <T> T decode(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(byteArrayInputStream);
            Object o = in.readObject();
            return (T) o;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {}
        }
        return null;
    }
}
