package net.atlas.combatify.mixin;

import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PickaxeItem.class)
public class PickaxeItemMixin extends DiggerItemMixin {
	public PickaxeItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public WeaponType combatify$getWeaponType() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(this);
		if (configurableItemData != null) {
			WeaponType type = configurableItemData.weaponStats().weaponType();
			if (type != null)
				return type;
		}
		return WeaponType.PICKAXE;
	}

}
