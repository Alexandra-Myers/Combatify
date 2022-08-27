package net.alexandra.atlas.atlas_combat.mixin;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.network.chat.Component;
import net.rizecookey.cookeymod.config.option.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(Option.class)
public abstract class OptionMixin<T> implements OptionAccessor {
	@Shadow
	Supplier<AbstractConfigListEntry<?>> entry;

	@Shadow
	public void set(T value) {
	}

	@Shadow
	public String getTranslationKey() {
		return null;
	}

	@Shadow
	public Optional<Component[]> getTooltip(String translationId) {
		return Optional.empty();
	}

	@Shadow
	public T get() {
		return null;
	}

	@Override
	public Supplier<AbstractConfigListEntry<?>> getEntry() {
		return entry;
	}

	@Override
	public void setEntry(Supplier<AbstractConfigListEntry<?>> entry) {
		this.entry = entry;
	}
}
