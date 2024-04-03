package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tiers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(Tiers.class)
public class TiersMixin {
	@Mutable
	@Shadow
	@Final
	private float damage;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void modifyDamage(String string, int i, TagKey tagKey, int j, float f, float g, int k, Supplier supplier, CallbackInfo ci) {
		if (damage > 0 && Combatify.CONFIG.ctsAttackBalancing()) {
			damage -= 1;
		}
	}
}
