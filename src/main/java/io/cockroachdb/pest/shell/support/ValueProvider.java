package io.cockroachdb.pest.shell.support;

@FunctionalInterface
public interface ValueProvider<T> {
    Object getValue(T object, int column);
}
