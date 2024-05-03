package net.atlas.combatify.extensions;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public interface ExtendedTier extends Tier {
	int getLevel();
	String baseTierName();

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
	static ExtendedTier create(int level, int enchantLevel, int uses, float damage, float speed, Ingredient repairIngredient, TagKey<Block> incorrect, String baseTier) {
		return new ExtendedTier() {
			@Override
			public int getLevel() {
				return level;
			}

			@Override
			public String baseTierName() {
				return baseTier;
			}

			@Override
			public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
				return incorrect;
			}

			@Override
			public int getUses() {
				return uses;
			}

			@Override
			public float getSpeed() {
				return speed;
			}

			@Override
			public float getAttackDamageBonus() {
				return damage;
			}

			@Override
			public int getEnchantmentValue() {
				return enchantLevel;
			}

			@Override
			public @NotNull Ingredient getRepairIngredient() {
				return repairIngredient;
			}
		};
	}
}
