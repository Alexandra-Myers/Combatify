package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HoneyBottleItem.class)
public class HoneyBottleItemMixin {

	@Unique
	public final int maxUseDuration = AtlasConfig.honeyBottleUseDuration;

	/**
	 * @author zOnlyKroks
	 * @reason because
	 */
	@Overwrite()
	public int getUseDuration(ItemStack stack) {
		return maxUseDuration;
	}

}
