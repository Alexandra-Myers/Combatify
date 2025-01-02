package net.atlas.combatify.extensions;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface Tier {

	TagKey<Block> incorrectBlocksForDrops();

	int durability();

	float speed();

	float attackDamageBonus();

	int enchantmentValue();

	TagKey<Item> repairItems();

	default int combatify$weaponLevel() {
		throw new IllegalStateException("Extension has not been applied");
	}

	default int combatify$blockingLevel() {
		throw new IllegalStateException("Extension has not been applied");
	}
}
