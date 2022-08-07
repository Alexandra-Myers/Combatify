package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.enchantment.CustomEnchantmentHelper;
import net.alexandra.atlas.atlas_combat.extensions.PlayerShieldExtensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LivingEntity.class)
public class LivingEntityMixin{

	/**
	 * @author zOnlyKroks
	 */
	@Overwrite()
	public void blockedByShield(LivingEntity target) {
		target.knockback(0.5F, target.getX() - ((LivingEntity)(Object)this).getX(), target.getZ() - ((LivingEntity)(Object)this).getZ());
		if (((LivingEntity)(Object)this).getMainHandItem().getItem() instanceof AxeItem) {
			float var2 = 1.6F + (float) CustomEnchantmentHelper.getChopping(((LivingEntity) (Object)this)) * 0.5F;
			if(target instanceof PlayerShieldExtensions player) {
				player.customShieldInteractions(var2);
			}
		}
	}
}
