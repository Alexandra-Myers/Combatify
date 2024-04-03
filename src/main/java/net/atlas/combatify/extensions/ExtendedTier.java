package net.atlas.combatify.extensions;

import net.atlas.combatify.Combatify;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public interface ExtendedTier extends Tier {
	int getLevel();
	default Tier baseTier() {
		return Combatify.ITEMS.getTier(baseTierName());
	}
	String baseTierName();

	@Override
	default @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
		return switch (getLevel()) {
			case 0 -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
			case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
			case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
			case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
			case 4 -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
            default -> baseTier().getIncorrectBlocksForDrops();
        };
	}
	static int getLevelFromDefault(Tier tier) {
		if (tier instanceof Tiers tiers) {
			return switch (tiers) {
				case WOOD, GOLD -> 0;
				case STONE -> 1;
				case IRON -> 2;
				case DIAMOND -> 3;
				case NETHERITE -> 4;
			};
		}
		return 0;
	}
	static int getLevel(Tier tier) {
		if (tier instanceof ExtendedTier extendedTier)
			return extendedTier.getLevel();
		return getLevelFromDefault(tier);
	}
}
