package io.cockroachdb.pc.shell.support;

@FunctionalInterface
public interface ValueProvider<T> {
    Object getValue(T object, int column);
}
