package io.atomix.client.serializer;

public interface Serializer<T> {
    byte[] serialize(T object);
    T deserialize(byte[] bytes);
}
