package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.IBowItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends ProjectileWeaponItem implements IBowItem {

	private BowItemMixin(Properties properties) {
		super(properties);
	}
	@ModifyConstant(method = "releaseUsing", constant = @Constant(floatValue = 1.0F, ordinal = 0))
	public float releaseUsing(float constant, @Local(ordinal = 1) final int time) {
		return Combatify.CONFIG.bowUncertainty() * getFatigueForTime(time);
	}
	@Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I", ordinal = 1))
	public void releaseUsing1(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci, @Local(ordinal = 0) final AbstractArrow abstractArrow, @Local(ordinal = 1) final int time) {
		if(getFatigueForTime(time) > 0.5F)
			abstractArrow.setCritArrow(false);
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
