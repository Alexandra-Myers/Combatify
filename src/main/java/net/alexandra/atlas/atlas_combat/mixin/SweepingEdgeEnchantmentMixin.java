package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SweepingEdgeEnchantment.class)
public class SweepingEdgeEnchantmentMixin {

	/**
	 * @author zOnlyKroks
	 * @reason Change damage ratio accordingly
	 */
	@Overwrite
	public static float getSweepingDamageRatio(int lvl) {
		return 0.5F - 0.5F / (float)(lvl + 1);
	}
}
