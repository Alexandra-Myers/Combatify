package net.atlas.combatify.util.blocking.damage_parsers;

import com.mojang.serialization.MapCodec;

import net.atlas.combatify.util.blocking.ComponentModifier.DataSet;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;

import java.util.Collections;
import java.util.List;

public record Nullify(List<TagPredicate<DamageType>> requirements, boolean enforceAll) implements DamageParser {
	public static final DamageParser NULLIFY_EXPLOSIONS_AND_PROJECTILES = new Nullify(List.of(TagPredicate.is(DamageTypeTags.IS_EXPLOSION), TagPredicate.is(DamageTypeTags.IS_PROJECTILE)), false);
	public static final MapCodec<Nullify> CODEC = DamageParser.mapCodec(Nullify::new);
	@Override
	public float parse(float originalValue, DataSet protection, Holder<DamageType> damageType) {
		if (allAre(damageType)) return originalValue;
		return 0;
	}

	@Override
	public MapCodec<? extends DamageParser> codec() {
		return CODEC;
	}
}
