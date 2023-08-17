package net.atlas.combatify.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ArrayListExtensions<T> extends ArrayList<T> implements IterableExtensions<T> {
	public ArrayListExtensions(int initialCapacity) {
		super(initialCapacity);
	}
	public ArrayListExtensions() {
		super();
	}
	public ArrayListExtensions(Collection<? extends T> c) {
		super(c);
	}
    @SafeVarargs
    public final boolean addAll(T... entries) {
        return addAll(Arrays.asList(entries));
    }
    @SafeVarargs
    public final boolean addAll(Collection<T>... entries) {
        boolean bl = true;
        for(Collection<T> entry : entries) {
            bl &= addAll(entry);
        }
        return bl;
    }
    @SafeVarargs
    public final boolean addAll(int index, T... entries) {
        return addAll(index, Arrays.asList(entries));
    }
    @SafeVarargs
    public final boolean addAll(int index, Collection<T>... entries) {
        boolean bl = true;
        for(Collection<T> entry : entries) {
            bl &= addAll(index, entry);
        }
        return bl;
    }
    @SafeVarargs
    public final boolean removeAll(T... entries) {
        return removeAll(Arrays.asList(entries));
    }
    @SafeVarargs
    public final boolean removeAll(Collection<T>... entries) {
        boolean bl = true;
        for(Collection<T> entry : entries) {
            bl &= removeAll(entry);
        }
        return bl;
    }
    @SafeVarargs
    public final boolean removeAll(int index, T... entries) {
        return removeAll(index, Arrays.asList(entries));
    }
    @SafeVarargs
    public final boolean removeAll(int index, Collection<T>... entries) {
        boolean bl = true;
        for (Collection<T> entry : entries) {
            bl &= removeAll(index, entry);
        }
        return bl;
    }
}
