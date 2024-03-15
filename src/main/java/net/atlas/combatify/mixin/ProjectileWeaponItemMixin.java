package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.atlas.combatify.util.MethodHandler.getFatigueForTime;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
	@Inject(method = "createProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I", ordinal = 0))
	public void createProjectile(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl, CallbackInfoReturnable<Projectile> cir, @Local(ordinal = 0) AbstractArrow abstractArrow) {
		if(itemStack.getItem() instanceof BowItem && getFatigueForTime(itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) > 0.5F)
			abstractArrow.setCritArrow(false);
	}
}
