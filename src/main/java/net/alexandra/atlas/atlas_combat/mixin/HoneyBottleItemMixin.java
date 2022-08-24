package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;

@Mixin(HoneyBottleItem.class)
public class HoneyBottleItemMixin {

	@Unique
	public final int maxUseDuration =  AtlasCombat.helper.getInt(AtlasCombat.helper.generalJsonObject, "honeyBottleUseDuration");

	/**
	 * @author zOnlyKroks
	 * @reason because
	 */
	@Overwrite()
	public int getUseDuration(ItemStack stack) {
		return maxUseDuration;
	}

}
