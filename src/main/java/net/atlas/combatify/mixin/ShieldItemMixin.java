package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShieldItem.class)
public class ShieldItemMixin extends Item implements ItemExtensions {
    public ShieldItemMixin(Properties properties) {
        super(properties);
    }

	@Override
	public BlockingType getBlockingType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.blockingType != null)
				return configurableItemData.blockingType;
			WeaponType type;
			if ((type = configurableItemData.type) != null && Combatify.ITEMS.configuredWeapons.containsKey(type)) {
				BlockingType blockingType = Combatify.ITEMS.configuredWeapons.get(type).blockingType;
				if (blockingType != null)
					return blockingType;
			}
		}
		return Combatify.registeredTypes.get("shield");
	}

	@Override
	public Item self() {
		return this;
	}
}
