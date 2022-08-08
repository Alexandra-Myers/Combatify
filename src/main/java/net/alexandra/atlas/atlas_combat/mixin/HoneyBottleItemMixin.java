package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(HoneyBottleItem.class)
public class HoneyBottleItemMixin {

	/**
	 * @author zOnlyKroks
	 * @reason because
	 */
	@Overwrite()
	public int getUseDuration(ItemStack stack) {
		return 20;
	}

}
