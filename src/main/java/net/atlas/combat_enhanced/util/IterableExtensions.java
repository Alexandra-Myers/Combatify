package net.atlas.combat_enhanced.util;

public interface IterableExtensions<T> extends Iterable<T> {
    default Iterable<T> asIterable() {
        return this;
    }
}
