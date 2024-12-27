package net.atlas.combatify.util.blocking.damage_parsers;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;

import java.util.Optional;

public record Nullify() implements DamageParser {
	public static final ConditionalEffect<DamageParser> NULLIFY_ALL = new ConditionalEffect<>(new Nullify(), Optional.empty());
	public static final ConditionalEffect<DamageParser> NULLIFY_EXPLOSIONS_AND_PROJECTILES = new ConditionalEffect<>(new Nullify(), Optional.of(AnyOfCondition.anyOf(
		DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_EXPLOSION))),
			DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))))
		.build()));
	public static final MapCodec<Nullify> CODEC = MapCodec.unit(Nullify::new);
	@Override
	public float parse(float originalValue, float protection) {
		return originalValue;
	}

	@Override
	public MapCodec<? extends DamageParser> codec() {
		return CODEC;
	}
}
