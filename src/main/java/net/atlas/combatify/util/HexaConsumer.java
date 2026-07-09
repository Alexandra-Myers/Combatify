package net.atlas.combatify.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface HexaConsumer<H, I, J, K, L, M> {
    void accept(H var1, I var2, J var3, K var4, L var5, M var6);

	default HexaConsumer<H, I, J, K, L, M> andThen(HexaConsumer<? super H, ? super I, ? super J, ? super K, ? super L, ? super M> after) {
		return (h, i, j, k, l, m) -> {
			this.accept(h, i, j, k, l, m);
			after.accept(h, i, j, k, l, m);
		};
	}

	default <N> SeptaConsumer<H, I, J, K, L, M, N> above(Consumer<N> after) {
		return (h, i, j, k, l, m, n) -> {
			this.accept(h, i, j, k, l, m);
			after.accept(n);
		};
	}

	default <N> SeptaConsumer<H, I, J, K, L, M, N> above() {
		return (h, i, j, k, l, m, n) -> {
			this.accept(h, i, j, k, l, m);
		};
	}
}
