package net.atlas.combatify.mixin;

import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.item.HoeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HoeItem.class)
public class HoeItemMixin extends DiggerItemMixin {
	public HoeItemMixin(Properties properties) {
		super(properties);
	}

	@Override
	public WeaponType combatify$getWeaponType() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(this);
		if (configurableItemData != null) {
			WeaponType type = configurableItemData.weaponStats().weaponType();
			if (type != null)
				return type;
		}
		return WeaponType.HOE;
	}

}
