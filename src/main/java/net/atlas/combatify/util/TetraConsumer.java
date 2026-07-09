package net.atlas.combatify.util;

import java.util.Objects;

@FunctionalInterface
public interface TetraConsumer<T, U, V, W> {
    void accept(T var1, U var2, V var3, W var4);

    default TetraConsumer<T, U, V, W> andThen(TetraConsumer<? super T, ? super U, ? super V, ? super W> after) {
        Objects.requireNonNull(after);
        return (t, u, v, w) -> {
            this.accept(t, u, v, w);
            after.accept(t, u, v, w);
        };
    }
}
