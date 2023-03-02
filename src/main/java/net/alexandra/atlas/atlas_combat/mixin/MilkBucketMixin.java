package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MilkBucketItem.class)
public class MilkBucketMixin {

	@Unique
	public final int useDuration = AtlasConfig.milkBucketUseDuration;

	/**
	 * @author zOnlyKroks
	 * @reason because
	 */
	@Overwrite
	public int getUseDuration(ItemStack stack) {
		return useDuration;
	}
}
