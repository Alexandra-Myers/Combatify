package net.atlas.combatify.mixin.compatibility.polymer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.atlas.combatify.component.CustomDataComponents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@ModSpecific("polymer-core")
@Mixin(PolymerComponent.class)
public interface PolymerComponentMixin {
	@ModifyReturnValue(method = "canSync", at = @At("RETURN"))
	private static boolean enableSyncForModded(boolean original, @Local(argsOnly = true) PacketContext packetContext, @Local(argsOnly = true) DataComponentType<?> key) {
		if (packetContext == null) return original;
		GameProfile gameProfile = packetContext.get(PacketContext.GAME_PROFILE);
		return original || (gameProfile != null && Combatify.moddedPlayers.contains(gameProfile.id()) && isCombatifyComponent(key));
	}
	@Unique
	private static boolean isCombatifyComponent(DataComponentType<?> dataComponentType) {
		return CustomDataComponents.combatifyComponents.contains(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(dataComponentType));
	}
}
