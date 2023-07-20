package net.alexandra.atlas.atlas_combat.mixin;

import io.wispforest.owo.config.Option;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Option.class)
public interface OwoOptionAccessor<T> {
	@Invoker(remap = false, value = "read")
	T readFromBuffer(FriendlyByteBuf buf);
}
