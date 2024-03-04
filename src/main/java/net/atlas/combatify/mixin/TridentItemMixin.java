package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.WeaponWithType;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TridentItem.class)
public class TridentItemMixin extends Item implements ItemExtensions, WeaponWithType {

	public TridentItemMixin(Item.Properties properties) {
		super(properties);
	}
	@Override
	public ItemAttributeModifiers modifyAttributeModifiers() {
		if (!Combatify.CONFIG.weaponTypesEnabled())
			return null;
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		getWeaponType().addCombatAttributes(Tiers.NETHERITE, builder);
		return builder.build();
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}

	@Override
	public WeaponType getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.TRIDENT;
	}
	@Override
	public double getChargedAttackBonus() {
		Item item = this;
		double chargedBonus = getWeaponType().getChargedReach();
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}

	@Override
	public boolean canSweep() {
		Item item = this;
		boolean canSweep = getWeaponType().canSweep();
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.canSweep != null)
				canSweep = configurableItemData.canSweep;
		}
		return canSweep;
	}

	@Override
	public BlockingType getBlockingType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.blockingType != null) {
				return configurableItemData.blockingType;
			}
		}
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(getWeaponType())) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(getWeaponType());
			if (configurableWeaponData.blockingType != null) {
				return configurableWeaponData.blockingType;
			}
		}
		return Combatify.EMPTY;
	}

	@Override
	public double getPiercingLevel() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.piercingLevel != null) {
				return configurableItemData.piercingLevel;
			}
		}
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(getWeaponType())) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(getWeaponType());
			if (configurableWeaponData.piercingLevel != null) {
				return configurableWeaponData.piercingLevel;
			}
		}
		return 0;
	}
}
