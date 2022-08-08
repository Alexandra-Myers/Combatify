package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MilkBucketItem.class)
public class MilkBucketMixin {

	/**
	 * @author zOnlyKroks
	 * @reason because
	 */
	@Overwrite
	public int getUseDuration(ItemStack stack) {
		return 20;
	}
}
