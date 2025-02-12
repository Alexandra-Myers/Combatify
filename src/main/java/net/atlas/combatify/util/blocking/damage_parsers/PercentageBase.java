package net.atlas.combatify.util.blocking.damage_parsers;

import com.mojang.serialization.MapCodec;

import net.atlas.combatify.util.blocking.ComponentModifier.DataSet;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;

import java.util.Collections;
import java.util.List;

public record PercentageBase(List<TagPredicate<DamageType>> requirements, boolean enforceAll) implements DamageParser {
	public static final DamageParser ALL = new PercentageBase(Collections.emptyList(), true);
	public static final DamageParser IGNORE_EXPLOSIONS_AND_PROJECTILES = new PercentageBase(List.of(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION), TagPredicate.isNot(DamageTypeTags.IS_PROJECTILE)), true);
	public static final MapCodec<PercentageBase> CODEC = DamageParser.mapCodec(PercentageBase::new);

	@Override
	public float parse(float originalValue, DataSet protection, Holder<DamageType> damageType) {
		float subVal = originalValue - protection.addValue();
		subVal = Math.max(subVal, 0);
		if (allAre(damageType)) return Math.min(originalValue, protection.addValue() + subVal * protection.multiplyValue());
		return 0;
	}

	@Override
	public MapCodec<? extends DamageParser> codec() {
		return CODEC;
	}
}
