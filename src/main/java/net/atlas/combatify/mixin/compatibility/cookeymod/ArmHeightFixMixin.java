package net.atlas.combatify.mixin.compatibility.cookeymod;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.rizecookey.cookeymod.CookeyMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@ModSpecific("cookeymod")
@Mixin(ItemInHandRenderer.class)
public class ArmHeightFixMixin {
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
	public float modifyArmHeight(float strengthScale) {
		if (Combatify.CONFIG.chargedAttacks())
			strengthScale *= 0.5f;
		if (CombatifyClient.augmentedArmHeight.get())
			strengthScale = strengthScale * strengthScale * strengthScale * 0.25F + 0.75F;
		double offset = CookeyMod.getInstance().getConfig().hudRendering().attackCooldownHandOffset().get();
		return (float) (strengthScale * (1 - offset) + offset);
	}
}
