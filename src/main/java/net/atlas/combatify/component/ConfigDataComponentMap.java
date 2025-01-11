package net.atlas.combatify.component;

import net.atlas.combatify.mixin.accessor.PatchedDataComponentMapAccessor;
import net.minecraft.core.component.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ConfigDataComponentMap implements DataComponentMap {
	public final PatchedDataComponentMap core;

	public ConfigDataComponentMap(DataComponentMap prototype, DataComponentPatch patch) {
		core = new PatchedDataComponentMap(prototype);
		core.applyPatch(patch);
	}

	@Nullable
	public <T> T get(DataComponentType<? extends T> dataComponentType) {
		return core.get(dataComponentType);
	}

	public Set<DataComponentType<?>> keySet() {
		return core.keySet();
	}

	public Iterator<TypedDataComponent<?>> iterator() {
		return core.iterator();
	}

	public int size() {
		return core.size();
	}

	public DataComponentPatch asPatch() {
		return core.asPatch();
	}

	public DataComponentMap getPrototype() {
		return PatchedDataComponentMapAccessor.class.cast(core).getPrototype();
	}

	public boolean equals(Object object) {
		if (this == object) return true;
		if (object instanceof ConfigDataComponentMap configDataComponentMap) return this.core.equals(configDataComponentMap.core);
		return false;
	}

	public int hashCode() {
		return this.core.hashCode();
	}

	public String toString() {
		return "Combatify ConfigDataComponentMap" + core.toString();
	}
}
