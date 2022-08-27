package net.alexandra.atlas.atlas_combat.mixin;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.rizecookey.cookeymod.config.option.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin({Option.class})
public interface OptionAccessor {

	@Accessor
	Supplier<AbstractConfigListEntry<?>> getEntry();
	@Accessor
	void setEntry(Supplier<AbstractConfigListEntry<?>> entry);
}
