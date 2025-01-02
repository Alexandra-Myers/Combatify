package net.atlas.combatify.util.blocking.damage_parsers;

import com.mojang.serialization.MapCodec;
import net.atlas.combatify.critereon.CustomLootContextParamSets;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public record Nullify(Optional<LootItemCondition> requirements) implements DamageParser {
	public static final DamageParser NULLIFY_ALL = new Nullify(Optional.empty());
	public static final DamageParser NULLIFY_EXPLOSIONS_AND_PROJECTILES = new Nullify(Optional.of(AnyOfCondition.anyOf(
			DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_EXPLOSION))),
			DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))))
		.build()));
	public static final MapCodec<Nullify> CODEC = ConditionalEffect.conditionCodec(CustomLootContextParamSets.BLOCKED_DAMAGE).optionalFieldOf("requirements").xmap(Nullify::new, Nullify::requirements);
	@Override
	public float parse(float originalValue, float protection, LootContext context) {
		if (requirements.isEmpty() || requirements.get().test(context)) return originalValue;
		return 0;
	}

	@Override
	public MapCodec<? extends DamageParser> codec() {
		return CODEC;
	}
}
