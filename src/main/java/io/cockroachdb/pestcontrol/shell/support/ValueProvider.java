package io.cockroachdb.pestcontrol.shell.support;

@FunctionalInterface
public interface ValueProvider<T> {
    Object getValue(T object, int column);
}
