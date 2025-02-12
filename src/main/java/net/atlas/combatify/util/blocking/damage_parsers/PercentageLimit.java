package net.atlas.combatify.util.blocking.damage_parsers;

import com.mojang.serialization.MapCodec;

import net.atlas.combatify.util.blocking.ComponentModifier.DataSet;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageType;

import java.util.Collections;
import java.util.List;

public record PercentageLimit(List<TagPredicate<DamageType>> requirements, boolean enforceAll) implements DamageParser {
	public static final DamageParser ALL = new PercentageLimit(Collections.emptyList(), true);
	public static final MapCodec<PercentageLimit> CODEC = DamageParser.mapCodec(PercentageLimit::new);
	@Override
	public float parse(float originalValue, DataSet protection, Holder<DamageType> damageType) {
		if (!allAre(damageType)) return 0;

		float subVal = originalValue - protection.addValue();
		subVal = Math.max(subVal, 0);
		return Math.min(protection.addValue() + Math.max(subVal - ((subVal + Math.clamp(2 * protection.multiplyValue(), 0, 1)) * (1 - protection.multiplyValue())), 0), originalValue);
	}

	@Override
	public MapCodec<? extends DamageParser> codec() {
		return CODEC;
	}
}
