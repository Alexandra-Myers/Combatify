package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxeItem.class)
public class AxeItemMixin extends DiggerItemMixin {
	public AxeItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public WeaponType getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.AXE;
	}
}
