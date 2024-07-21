package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.world.entity.LivingEntity;
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
	@Inject(method = "createProjectile", at = @At(value = "HEAD"))
	public void createProjectile(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl, CallbackInfoReturnable<Projectile> cir, @Local(ordinal = 0, argsOnly = true) LocalBooleanRef blAlt) {
		if (itemStack.getItem() instanceof BowItem && getFatigueForTime(itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) > 0.5F)
			blAlt.set(false);
	}
}
