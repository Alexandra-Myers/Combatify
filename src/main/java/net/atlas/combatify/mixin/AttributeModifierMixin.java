package net.atlas.combatify.mixin;

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

@Mixin(AttributeModifier.class)
public class AttributeModifierMixin {
	@Shadow
	@Mutable
	@Final
	private double amount;

	@Inject(method = "<init>(Ljava/util/UUID;Ljava/lang/String;DLnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;)V", at = @At(value = "RETURN"))
	private void injectChanges(UUID uUID, String string, double d, AttributeModifier.Operation operation, CallbackInfo ci) {
		if (uUID == Item.BASE_ATTACK_SPEED_UUID) {
			if (d >= 0) {
				amount = 1;
			} else if (d >= -1) {
				amount = 0.5;
			} else if (d == -2) {
				amount = 0.0;
			} else if (d >= -2.5) {
				amount = 0.5;
			} else if (d > -3) {
				amount = 0.0;
			} else if (d > -3.5) {
				amount = -0.5;
			} else {
				amount = -1;
			}
		}
	}
}
