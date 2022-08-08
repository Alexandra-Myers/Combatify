package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin implements ItemExtensions {

	@Override
	public void setStackSize(int stackSize) {
		((Item) (Object)this).maxStackSize = stackSize;
	}
}
