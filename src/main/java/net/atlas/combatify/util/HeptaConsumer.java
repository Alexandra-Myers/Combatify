package net.atlas.combatify.util;

@FunctionalInterface
public interface HeptaConsumer<H, I, J, K, L, M, N> {
    void accept(H var1, I var2, J var3, K var4, L var5, M var6, N var7);

	default HeptaConsumer<H, I, J, K, L, M, N> andThen(HeptaConsumer<? super H, ? super I, ? super J, ? super K, ? super L, ? super M, ? super N> after) {
		return (h, i, j, k, l, m, n) -> {
			this.accept(h, i, j, k, l, m, n);
			after.accept(h, i, j, k, l, m, n);
		};
	}
}
