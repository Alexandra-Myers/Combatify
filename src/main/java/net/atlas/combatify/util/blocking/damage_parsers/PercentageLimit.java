package net.atlas.combatify.util.blocking.damage_parsers;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.enchantment.ConditionalEffect;

import java.util.Optional;

public record PercentageLimit() implements DamageParser {
	public static final ConditionalEffect<DamageParser> ALL = new ConditionalEffect<>(new PercentageLimit(), Optional.empty());
	public static final MapCodec<PercentageLimit> CODEC = MapCodec.unit(PercentageLimit::new);
	@Override
	public float parse(float originalValue, float protection) {
		float protectionFromPercentage = protection / 100F;
		return Math.min(originalValue - ((originalValue + Math.clamp(2 * protection, 0, 1)) * (1 - protectionFromPercentage)), originalValue);
	}

	@Override
	public MapCodec<? extends DamageParser> codec() {
		return CODEC;
	}
}
