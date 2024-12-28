package net.atlas.combatify.util.blocking.damage_parsers;

import com.mojang.serialization.MapCodec;
import net.atlas.combatify.critereon.CustomLootContextParamSets;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public record PercentageLimit(Optional<LootItemCondition> requirements) implements DamageParser {
	public static final DamageParser ALL = new PercentageLimit(Optional.empty());
	public static final MapCodec<PercentageLimit> CODEC = ConditionalEffect.conditionCodec(CustomLootContextParamSets.BLOCKED_DAMAGE).optionalFieldOf("requirements").xmap(PercentageLimit::new, PercentageLimit::requirements);
	@Override
	public float parse(float originalValue, float protection, LootContext context) {
		if (requirements.isPresent() && !requirements.get().test(context)) return 0;
		float protectionFromPercentage = protection / 100F;
		return Math.min(originalValue - ((originalValue + Math.clamp(2 * protection, 0, 1)) * (1 - protectionFromPercentage)), originalValue);
	}

	@Override
	public MapCodec<? extends DamageParser> codec() {
		return CODEC;
	}
}
