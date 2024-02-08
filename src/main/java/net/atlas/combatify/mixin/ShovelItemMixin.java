package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShovelItem.class)
public class ShovelItemMixin extends DiggerItemMixin {
	public ShovelItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public WeaponType getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.SHOVEL;
	}

}
