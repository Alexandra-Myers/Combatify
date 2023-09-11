package net.atlas.combatify.util;

public class UtilClass<T> {
	public static Thread renderingThread = new Thread() {
		public void run() {
			super.run();
		}
	};
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
	public static boolean isAnyNull(Object... objects) {
		boolean bl = false;
		for (Object object : objects) {
			bl |= object == null;
		}
		return bl;
	}
	public static boolean isAnyNonNull(Object... objects) {
		boolean bl = false;
		for (Object object : objects) {
			bl |= object != null;
		}
		return bl;
	}
}
