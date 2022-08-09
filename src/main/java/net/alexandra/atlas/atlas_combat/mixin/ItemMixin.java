package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Item.class)
public class ItemMixin implements ItemExtensions {

	@Override
	public void setStackSize(int stackSize) {
		((Item) (Object)this).maxStackSize = stackSize;
	}

	/**
	 * @author zOnlyKroks
	 * @reason doesnt hurt other mods
	 */
	@Overwrite
	public int getUseDuration(ItemStack stack) {
		if (stack.getItem() instanceof BowlFoodItem) {
			return 20;
		}else if (stack.getItem().isEdible()) {
			return ((Item) (Object)this).getFoodProperties().isFastFood() ? 16 : 32;
		} else {
			return 0;
		}
	}
}
