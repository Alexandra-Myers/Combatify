package net.atlas.combatify.mixin.compatibility.polymer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(PolymerComponent.class)
public interface PolymerComponentMixin {
	@ModifyReturnValue(method = "canSync", at = @At("RETURN"))
	private static boolean enableSyncForModded(boolean original, @Local(argsOnly = true) PacketContext packetContext, @Local(argsOnly = true) DataComponentType<?> key) {
		return original || (packetContext.getPlayer() != null && Combatify.moddedPlayers.contains(packetContext.getPlayer().getUUID()) && isCombatifyComponent(key));
	}
	@Unique
	private static boolean isCombatifyComponent(DataComponentType<?> dataComponentType) {
		return CustomDataComponents.combatifyComponents.contains(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(dataComponentType));
	}
}
