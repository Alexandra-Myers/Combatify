package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PickaxeItem.class)
public class PickaxeItemMixin extends DiggerItemMixin {
	public PickaxeItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public WeaponType getWeaponType() {
		if(Combatify.CONFIG != null && Combatify.CONFIG.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.CONFIG.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.PICKAXE;
	}

}
