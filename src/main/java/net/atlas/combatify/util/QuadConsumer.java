package net.atlas.combatify.util;

import java.util.Objects;

@FunctionalInterface
public interface QuadConsumer<T, U, V, W> {
    void accept(T var1, U var2, V var3, W var4);

    default QuadConsumer<T, U, V, W> andThen(QuadConsumer<? super T, ? super U, ? super V, ? super W> after) {
        Objects.requireNonNull(after);
        return (t, u, v, w) -> {
            this.accept(t, u, v, w);
            after.accept(t, u, v, w);
        };
    }
}
