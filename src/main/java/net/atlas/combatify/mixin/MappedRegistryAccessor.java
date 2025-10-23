package net.atlas.combatify.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor<T> {

	@Accessor
	Map<ResourceLocation, Holder.Reference<T>> getByLocation();

	@Accessor
	Map<ResourceKey<T>, Holder.Reference<T>> getByKey();

}
