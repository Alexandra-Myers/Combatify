package net.atlas.combatify.item;

import net.atlas.combatify.component.CustomDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;

public class LongSwordItem extends Item {
	public final /*? >=1.21.4 {*/ToolMaterial/*?} <1.21.4 {*/ /*Tier *//*?}*/ toolMaterial;
	public LongSwordItem(/*? >=1.21.4 {*/ToolMaterial/*?} <1.21.4 {*/ /*Tier *//*?}*/ toolMaterial, int weaponLevel, Properties properties) {
		super(toolMaterial.applySwordProperties(properties, 0, 0).component(DataComponents.WEAPON, new Weapon(1, 2F)).component(CustomDataComponents.PIERCING_LEVEL.get(), piercingLevelForTier(weaponLevel)).attributes(baseAttributeModifiers(weaponLevel, toolMaterial)));
		this.toolMaterial = toolMaterial;
	}

	public static ItemAttributeModifiers baseAttributeModifiers(int weaponLevel, /*? >=1.21.4 {*/ToolMaterial/*?} <1.21.4 {*/ /*Tier *//*?}*/ tier) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		WeaponType.LONGSWORD.addCombatAttributes(weaponLevel, tier, builder);
		return builder.build();
	}

	public static float piercingLevelForTier(int weaponLevel) {
		return weaponLevel >= 4 ? 0.2F
			: weaponLevel <= 1 ? 0
			: (0.1F * (weaponLevel - 1));
	}
}
