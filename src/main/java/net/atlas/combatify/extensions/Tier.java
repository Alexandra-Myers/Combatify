package net.atlas.combatify.extensions;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

public interface Tier {

	TagKey<Block> incorrectBlocksForDrops();

	int durability();

	float speed();

	float attackDamageBonus();

	int enchantmentValue();

	TagKey<Item> repairItems();

	int level();

	ToolMaterial asMaterial();
}
