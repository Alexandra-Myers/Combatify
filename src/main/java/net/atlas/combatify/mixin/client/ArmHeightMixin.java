package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.annotation.mixin.Incompatible;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Incompatible("cookeymod")
@Mixin(ItemInHandRenderer.class)
public class ArmHeightMixin {
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
	public float modifyArmHeight(float strengthScale) {
		if (Combatify.CONFIG.chargedAttacks())
			strengthScale *= 0.5f;
		if (CombatifyClient.augmentedArmHeight.get())
			strengthScale = strengthScale * strengthScale * strengthScale * 0.25F + 0.75F;
		return strengthScale;
	}
}
