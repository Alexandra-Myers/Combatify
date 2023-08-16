package net.atlas.combat_enhanced.mixin;

import net.atlas.combat_enhanced.CombatEnhanced;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Supplier;

@Mixin(AttributeModifier.class)
public class AttributeModifierMixin {
	@Shadow
	@Mutable
	@Final
	private double amount;

	@Inject(method = "<init>(Ljava/util/UUID;Ljava/util/function/Supplier;DLnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;)V", at = @At(value = "RETURN"))
	private void injectChanges(UUID uUID, Supplier<String> supplier, double d, AttributeModifier.Operation operation, CallbackInfo ci) {
		if (uUID == Item.BASE_ATTACK_SPEED_UUID) {
			if(d >= 0) {
				amount = CombatEnhanced.CONFIG.fastestToolAttackSpeed();
			} else if(d >= -1) {
				amount = CombatEnhanced.CONFIG.fastToolAttackSpeed();
			} else if(d == -2) {
				amount = CombatEnhanced.CONFIG.defaultAttackSpeed();
			} else if(d >= -2.5) {
				amount = CombatEnhanced.CONFIG.fastToolAttackSpeed();
			} else if(d > -3) {
				amount = CombatEnhanced.CONFIG.defaultAttackSpeed();
			} else if (d > -3.5) {
				amount = CombatEnhanced.CONFIG.slowToolAttackSpeed();
			} else {
				amount = CombatEnhanced.CONFIG.slowestToolAttackSpeed();
			}
		}
	}
}
