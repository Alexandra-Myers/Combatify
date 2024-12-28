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

public record Direct(Optional<LootItemCondition> requirements) implements DamageParser {
	public static final DamageParser IGNORE_EXPLOSIONS_AND_PROJECTILES = new Direct(Optional.of(DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType()
			.tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
			.tag(TagPredicate.isNot(DamageTypeTags.IS_PROJECTILE)))
		.build()));
	public static final MapCodec<Direct> CODEC = ConditionalEffect.conditionCodec(CustomLootContextParamSets.BLOCKED_DAMAGE).optionalFieldOf("requirements").xmap(Direct::new, Direct::requirements);
	@Override
	public float parse(float originalValue, float protection, LootContext context) {
		if (requirements.isEmpty() || requirements.get().test(context)) return Math.min(originalValue, protection);
		return 0;
	}

	@Override
	public MapCodec<? extends DamageParser> codec() {
		return CODEC;
	}
}
