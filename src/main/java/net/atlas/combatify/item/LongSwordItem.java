package net.atlas.combatify.item;

import net.atlas.combatify.component.CustomDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;

public class LongSwordItem extends Item {
	public final ToolMaterial toolMaterial;
	public LongSwordItem(ToolMaterial toolMaterial, int weaponLevel, Properties properties) {
		super(toolMaterial.applySwordProperties(properties, 0, 0).component(DataComponents.WEAPON, new Weapon(1, 2F)).component(CustomDataComponents.PIERCING_LEVEL, piercingLevelForTier(weaponLevel)).attributes(baseAttributeModifiers(weaponLevel, toolMaterial)));
		this.toolMaterial = toolMaterial;
	}

	public static ItemAttributeModifiers baseAttributeModifiers(int weaponLevel, ToolMaterial tier) {
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
