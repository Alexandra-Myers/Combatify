package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShieldItem.class)
public class ShieldItemMixin extends Item implements ItemExtensions {
    public ShieldItemMixin(Properties properties) {
        super(properties);
    }

	@Override
	public BlockingType combatify$getBlockingType() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(this);
		if (configurableItemData != null) {
			if (configurableItemData.blockingType != null)
				return configurableItemData.blockingType;
			WeaponType type;
			ConfigurableWeaponData configurableWeaponData;
			if ((type = configurableItemData.type) != null && (configurableWeaponData = MethodHandler.forWeapon(type)) != null) {
				BlockingType blockingType = configurableWeaponData.blockingType;
				if (blockingType != null)
					return blockingType;
			}
		}
		return Combatify.registeredTypes.get("shield");
	}

	@Override
	public Item combatify$self() {
		return this;
	}
}
