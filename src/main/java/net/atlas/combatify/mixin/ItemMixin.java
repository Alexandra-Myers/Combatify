package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions {

	@Override
	public void setStackSize(int stackSize) {
		((Item) (Object)this).maxStackSize = stackSize;
	}

	@Override
	public double getChargedAttackBonus() {
		Item item = Item.class.cast(this);
		double chargedBonus = 1.0;
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.type != null)
				if (Combatify.ITEMS.configuredWeapons.containsKey(configurableItemData.type))
					if (Combatify.ITEMS.configuredWeapons.get(configurableItemData.type).chargedReach != null)
						chargedBonus = Combatify.ITEMS.configuredWeapons.get(configurableItemData.type).chargedReach;
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}

	@Override
	public BlockingType getBlockingType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(Item.class.cast(this))) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(Item.class.cast(this));
			if (configurableItemData.blockingType != null) {
				return configurableItemData.blockingType;
			}
			if (configurableItemData.type != null && Combatify.ITEMS.configuredWeapons.containsKey(configurableItemData.type)) {
				ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(configurableItemData.type);
				if (configurableWeaponData.blockingType != null) {
					return configurableWeaponData.blockingType;
				}
			}
		}
		return Combatify.EMPTY;
	}

	@Override
	public double getPiercingLevel() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(Item.class.cast(this))) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(Item.class.cast(this));
			if (configurableItemData.piercingLevel != null) {
				return configurableItemData.piercingLevel;
			}
			if (configurableItemData.type != null && Combatify.ITEMS.configuredWeapons.containsKey(configurableItemData.type)) {
				ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(configurableItemData.type);
				if (configurableWeaponData.piercingLevel != null) {
					return configurableWeaponData.piercingLevel;
				}
			}
		}
		return 0;
	}
}
