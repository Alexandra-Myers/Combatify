package net.atlas.combatify.util.blocking.damage_parsers;

import com.mojang.serialization.MapCodec;
import net.atlas.combatify.critereon.CustomLootContextParamSets;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public record Percentage(Optional<LootItemCondition> requirements) implements DamageParser {
	public static final DamageParser ALL = new Percentage(Optional.empty());
	public static final DamageParser IGNORE_EXPLOSIONS_AND_PROJECTILES = new Percentage(Optional.of(DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType()
			.tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
			.tag(TagPredicate.isNot(DamageTypeTags.IS_PROJECTILE)))
		.build()));
	public static final MapCodec<Percentage> CODEC = ConditionalEffect.conditionCodec(CustomLootContextParamSets.BLOCKED_DAMAGE).optionalFieldOf("requirements").xmap(Percentage::new, Percentage::requirements);
	@Override
	public float parse(float originalValue, float protection, LootContext context) {
		if (requirements.isPresent() && !requirements.get().test(context)) return 0;
		float protectionFromPercentage = protection / 100F;
		return originalValue * Math.min(protectionFromPercentage, 1);
	}

	@Override
	public MapCodec<? extends DamageParser> codec() {
		return CODEC;
	}
}
