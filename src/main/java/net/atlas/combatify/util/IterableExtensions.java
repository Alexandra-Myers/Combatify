package net.atlas.combatify.util;

public interface IterableExtensions<T> extends Iterable<T> {
    default Iterable<T> asIterable() {
        return this;
    }
}
