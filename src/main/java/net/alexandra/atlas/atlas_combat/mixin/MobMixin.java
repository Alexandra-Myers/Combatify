package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.alexandra.atlas.atlas_combat.extensions.IEnchantmentHelper;
import net.alexandra.atlas.atlas_combat.extensions.LivingEntityExtensions;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

	protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"))
	public float getDamageBonus(ItemStack itemStack, MobType mobType, @Local(ordinal = 0) Entity entity) {
		EnchantmentHelper helper = new EnchantmentHelper();
		return ((IEnchantmentHelper)helper).getDamageBonus(itemStack, (LivingEntity) entity);
	}
	@Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void redirectKnockback(LivingEntity instance, double strength, double x, double z) {
		((LivingEntityExtensions)instance).newKnockback(strength, x, z);
	}
}
