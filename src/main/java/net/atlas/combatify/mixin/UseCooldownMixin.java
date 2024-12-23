package net.atlas.combatify.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UseCooldown.class)
public abstract class UseCooldownMixin {
	@Shadow
	public abstract int ticks();

	@Inject(method = "apply", at = @At("TAIL"))
	public void injectAdditionalCooldownForMobs(ItemStack itemStack, LivingEntity livingEntity, CallbackInfo ci) {
		if (!(livingEntity instanceof Player)) livingEntity.combatify$getFallbackCooldowns().addCooldown(itemStack, ticks());
	}
}
