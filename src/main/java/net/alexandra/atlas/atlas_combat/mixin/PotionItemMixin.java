package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;

@Mixin(PotionItem.class)
public class PotionItemMixin {

	@Unique
	public final int useDuration = (int) AtlasCombat.helper.getValue(Collections.singleton("potionUseDuration")).value();


	/**
	 * @author zOnlyKroks
	 */
	@Overwrite
    public int getUseDuration(ItemStack stack)
    {
        return useDuration;
    }
}
