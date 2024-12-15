package net.atlas.combatify.extensions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.config.ItemConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ExtendedTier extends Tier {
	Codec<Tier> TIER_CODEC = Codec.STRING.xmap(ItemConfig::getTier, ItemConfig::getTierName);
	Codec<ExtendedTierImpl> BASE_CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.INT.optionalFieldOf("mining_level").forGetter(extendedTier -> Optional.of(extendedTier.getLevel())),
				Codec.INT.optionalFieldOf("enchant_level").forGetter(extendedTier -> Optional.of(extendedTier.getEnchantmentValue())),
				Codec.INT.optionalFieldOf("uses").forGetter(extendedTier -> Optional.of(extendedTier.getUses())),
				Codec.FLOAT.optionalFieldOf("attack_damage_bonus").forGetter(extendedTier -> Optional.of(extendedTier.getAttackDamageBonus())),
				Codec.FLOAT.optionalFieldOf("mining_speed").forGetter(extendedTier -> Optional.of(extendedTier.getSpeed())),
				Ingredient.CODEC.optionalFieldOf("repair_ingredient").forGetter(extendedTier -> Optional.of(extendedTier.getRepairIngredient())),
				Codec.withAlternative(TagKey.codec(Registries.BLOCK), TagKey.hashedCodec(Registries.BLOCK)).optionalFieldOf("incorrect_blocks").forGetter(extendedTier -> Optional.of(extendedTier.getIncorrectBlocksForDrops())),
				TIER_CODEC.fieldOf("base_tier").forGetter(ExtendedTierImpl::self))
			.apply(instance, ExtendedTier::create));
	Codec<ExtendedTierImpl> FULL_CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.INT.fieldOf("mining_level").forGetter(ExtendedTierImpl::getLevel),
				Codec.INT.fieldOf("enchant_level").forGetter(ExtendedTierImpl::getEnchantmentValue),
				Codec.INT.fieldOf("uses").forGetter(ExtendedTierImpl::getUses),
				Codec.FLOAT.fieldOf("attack_damage_bonus").forGetter(ExtendedTierImpl::getAttackDamageBonus),
				Codec.FLOAT.fieldOf("mining_speed").forGetter(ExtendedTierImpl::getSpeed),
				Ingredient.CODEC.fieldOf("repair_ingredient").forGetter(ExtendedTierImpl::getRepairIngredient),
				Codec.withAlternative(TagKey.codec(Registries.BLOCK), TagKey.hashedCodec(Registries.BLOCK)).fieldOf("incorrect_blocks").forGetter(ExtendedTierImpl::getIncorrectBlocksForDrops))
			.apply(instance, ExtendedTier::create));

	Codec<ExtendedTierImpl> CODEC = Codec.withAlternative(FULL_CODEC, BASE_CODEC);

	int getLevel();

	@SuppressWarnings("unused")
	ExtendedTier self();

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
	static ExtendedTierImpl create(Optional<Integer> level, Optional<Integer> enchantLevel, Optional<Integer> uses, Optional<Float> damage, Optional<Float> speed, Optional<Ingredient> repairIngredient, Optional<TagKey<Block>> incorrect, Tier baseTier) {
		return new ExtendedTierImpl(level.orElse(getLevel(baseTier)), enchantLevel.orElse(baseTier.getEnchantmentValue()), uses.orElse(baseTier.getUses()), damage.orElse(baseTier.getAttackDamageBonus()), speed.orElse(baseTier.getSpeed()), repairIngredient.orElse(baseTier.getRepairIngredient()), incorrect.orElse(baseTier.getIncorrectBlocksForDrops()));
	}
	static ExtendedTierImpl create(int level, int enchantLevel, int uses, float damage, float speed, Ingredient repairIngredient, TagKey<Block> incorrect) {
		return new ExtendedTierImpl(level, enchantLevel, uses, damage, speed, repairIngredient, incorrect);
	}
	record ExtendedTierImpl(int level, int enchantLevel, int uses, float damage, float speed, Ingredient repairIngredient, TagKey<Block> incorrect) implements ExtendedTier {
		@Override
		public int getLevel() {
			return level;
		}

		@Override
		public ExtendedTier self() {
			return this;
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
	}
}
