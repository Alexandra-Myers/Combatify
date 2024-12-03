package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistrySyncManager.class)
public class RegistrySyncManagerMixin {
	@ModifyExpressionValue(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"))
	private static ResourceLocation suppress(@Nullable ResourceLocation original) {
		if (original != null && (original.equals(ResourceLocation.tryParse("minecraft:generic.shield_disable_time")) || original.equals(ResourceLocation.tryParse("minecraft:generic.shield_disable_reduction"))))
			return null;
		return original;
	}
}
