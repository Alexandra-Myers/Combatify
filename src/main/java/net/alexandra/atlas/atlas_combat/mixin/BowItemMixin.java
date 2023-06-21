package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IBowItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends ProjectileWeaponItem implements IBowItem {

	@Unique
	private final float configUncertainty = AtlasCombat.CONFIG.bowUncertainty();
	@Unique
	private float fatigue;


	private BowItemMixin(Properties properties) {
		super(properties);
	}
	@Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
		int time = this.getUseDuration(stack) - remainingUseTicks;
		fatigue = getFatigueForTime(time);
	}
	@ModifyConstant(method = "releaseUsing", constant = @Constant(floatValue = 1.0F, ordinal = 0))
	public float releaseUsing1(float constant) {
		return configUncertainty * fatigue;
	}
	@Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setCritArrow(Z)V"), cancellable = true)
	public void releaseUsing2(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
		if(fatigue > 0.5F)
			ci.cancel();
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
