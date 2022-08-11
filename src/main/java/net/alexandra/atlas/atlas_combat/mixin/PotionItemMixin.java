package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @Overwrite
    public int getUseDuration(ItemStack stack)
    {
        return 20;
    }
}
