package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.ItemExtensions;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions {
	@Override
	public Item combatify$self() {
		return Item.class.cast(this);
	}
}
