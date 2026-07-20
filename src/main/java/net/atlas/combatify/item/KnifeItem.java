package net.atlas.combatify.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class KnifeItem extends Item {
	public final /*? >=1.21.4 {*/ToolMaterial/*?} <1.21.4 {*/ /*Tier *//*?}*/ toolMaterial;
	public KnifeItem(/*? >=1.21.4 {*/ToolMaterial/*?} <1.21.4 {*/ /*Tier *//*?}*/ toolMaterial, Properties properties) {
		super(toolMaterial.applySwordProperties(properties, 0, 0).attributes(baseAttributeModifiers(toolMaterial)));
		this.toolMaterial = toolMaterial;
	}

	public static ItemAttributeModifiers baseAttributeModifiers(/*? >=1.21.4 {*/ToolMaterial/*?} <1.21.4 {*/ /*Tier *//*?}*/ tier) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		WeaponType.KNIFE.addCombatAttributes(0, tier, builder);
		return builder.build();
	}
}
