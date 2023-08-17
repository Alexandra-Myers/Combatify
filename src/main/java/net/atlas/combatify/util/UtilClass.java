package net.atlas.combatify.util;

public class UtilClass<T> {
	public UtilClass() {

	}
	@SafeVarargs
	public final boolean compare(T comparatee, T... comparators) {
		boolean bl = true;
		for(T object : comparators) {
			bl &= comparatee == object;
		}
		return bl;
	}
}
