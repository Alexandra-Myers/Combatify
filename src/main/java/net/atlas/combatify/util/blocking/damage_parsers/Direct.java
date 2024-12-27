package net.atlas.combatify.util.blocking.damage_parsers;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;

import java.util.Optional;

public record Direct() implements DamageParser {
	public static final ConditionalEffect<DamageParser> IGNORE_EXPLOSIONS_AND_PROJECTILES = new ConditionalEffect<>(new Direct(), Optional.of(DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType()
			.tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
			.tag(TagPredicate.isNot(DamageTypeTags.IS_PROJECTILE)))
		.build()));
	public static final MapCodec<Direct> CODEC = MapCodec.unit(Direct::new);
	@Override
	public float parse(float originalValue, float protection) {
		return Math.min(originalValue, protection);
	}

	@Override
	public MapCodec<? extends DamageParser> codec() {
		return CODEC;
	}
}
