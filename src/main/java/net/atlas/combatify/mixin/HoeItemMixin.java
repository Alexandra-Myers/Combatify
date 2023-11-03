package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HoeItem.class)
public class HoeItemMixin extends DiggerItemMixin {
	public HoeItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public WeaponType combatify$getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.HOE;
	}
	@Override
	public double combatify$getChargedAttackBonus() {
		Item item = this;
		double chargedBonus = combatify$getWeaponType().getChargedReach();
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}

}
