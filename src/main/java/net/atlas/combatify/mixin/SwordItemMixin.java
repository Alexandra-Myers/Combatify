package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public class SwordItemMixin extends Item implements WeaponWithType {
	public SwordItemMixin(Properties properties) {
		super(properties);
	}

	@Override
	public BlockingType combatify$getBlockingType() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(this);
		if (configurableItemData != null) {
			BlockingType blockingType = configurableItemData.blocker().blockingType();
			if (blockingType != null)
				return blockingType;
		}
		ConfigurableWeaponData configurableWeaponData = MethodHandler.forWeapon(combatify$getWeaponType());
		if (configurableWeaponData != null) {
			BlockingType blockingType = configurableWeaponData.blockingType();
			if (blockingType != null)
				return blockingType;
		}
		return Combatify.registeredTypes.get("sword");
	}

	@Override
	public WeaponType combatify$getWeaponType() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(this);
		if (configurableItemData != null) {
			WeaponType type = configurableItemData.weaponStats().weaponType();
			if (type != null)
				return type;
		}
		return WeaponType.SWORD;
	}
}
