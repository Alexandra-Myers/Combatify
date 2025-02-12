package net.atlas.combatify.util.blocking.damage_parsers;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.combatify.util.blocking.BlockingTypeInit;
import net.atlas.combatify.util.blocking.ComponentModifier.DataSet;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;

public interface DamageParser {
	Codec<DamageParser> CODEC = BlockingTypeInit.DAMAGE_PARSER_TYPE_REG
		.byNameCodec()
		.dispatch(DamageParser::codec, mapCodec -> mapCodec);
	static void bootstrap(Registry<MapCodec<? extends DamageParser>> registry) {
		Registry.register(registry, "percentage_base", PercentageBase.CODEC);
		Registry.register(registry, "percentage_limit", PercentageLimit.CODEC);
		Registry.register(registry, "nullify", Nullify.CODEC);
	}
	float parse(float originalValue, DataSet protection, Holder<DamageType> damageType);

	MapCodec<? extends DamageParser> codec();

	List<TagPredicate<DamageType>> requirements();

	boolean enforceAll();

	default boolean allAre(Holder<DamageType> damageType) {
		if (requirements().isEmpty()) return true;
		boolean enforce = enforceAll();
		for (TagPredicate<DamageType> requirement : requirements()) {
			boolean matches = requirement.matches(damageType);
			if (matches != enforce) return matches;
		}
		return enforce;
	}

	public static <D extends DamageParser> MapCodec<D> mapCodec(BiFunction<List<TagPredicate<DamageType>>, Boolean, D> creator) {
		return RecordCodecBuilder.mapCodec(instance ->
			instance.group(TagPredicate.codec(Registries.DAMAGE_TYPE).listOf().optionalFieldOf("damage_types", Collections.emptyList()).forGetter(DamageParser::requirements),
				Codec.BOOL.optionalFieldOf("enforce_all", true).forGetter(DamageParser::enforceAll))
			.apply(instance, creator));
	}
}
