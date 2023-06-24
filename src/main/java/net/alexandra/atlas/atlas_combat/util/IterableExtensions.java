package net.alexandra.atlas.atlas_combat.util;

public interface IterableExtensions<T> extends Iterable<T> {
    default Iterable<T> asIterable() {
        return this;
    }
}
