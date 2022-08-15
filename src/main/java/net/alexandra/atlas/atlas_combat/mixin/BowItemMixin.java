package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IBowItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends ProjectileWeaponItem implements IBowItem {
	@Shadow
	public static float getPowerForTime(int i) {
		return 0;
	}

	float stupidNecessaryVariable;

	public BowItemMixin(Properties properties) {
		super(properties);
	}

	@ModifyConstant(method = "releaseUsing",constant = @Constant(floatValue = 1.0F))
	public float modifyUncertaintyConstant(float value) {
		return 0.25F * stupidNecessaryVariable;
	}
	@Inject(method = "releaseUsing", at = @At(value = "HEAD"))
	public void injectNewVariable(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
		stupidNecessaryVariable = getFatigueForTime(this.getUseDuration(stack) - remainingUseTicks);
	}
	@Inject(method = "releaseUsing", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void injectNewCrit(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, Player player, boolean bl, ItemStack itemStack, int i, float f, boolean bl2, ArrowItem arrowItem, AbstractArrow abstractArrow) {
		if(getPowerForTime(this.getUseDuration(stack) - remainingUseTicks) == 1.0F && getFatigueForTime(this.getUseDuration(stack) - remainingUseTicks) <= 0.5F){
			abstractArrow.setCritArrow(true);
		}
	}
	@Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setCritArrow(Z)V"))
	public void redirectCritArrow(AbstractArrow instance, boolean b) {
	}

	@Override
	public float getFatigueForTime(int f) {
		if (f < 60) {
			return 0.5F;
		} else {
			return f >= 200 ? 10.5F : 0.5F + 10.0F * (float)(f - 60) / 140.0F;
		}
	}
}
