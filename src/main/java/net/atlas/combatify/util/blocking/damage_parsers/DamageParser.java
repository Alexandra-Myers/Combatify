package net.atlas.combatify.util.blocking.damage_parsers;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.combatify.util.blocking.ComponentModifier.DataSet;
import net.atlas.defaulted.extension.LateBoundIdMapper;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public interface DamageParser {
	LateBoundIdMapper<ResourceLocation, MapCodec<? extends DamageParser>> ID_MAPPER = new LateBoundIdMapper<>();
	Codec<DamageParser> CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
		.dispatch(DamageParser::codec, mapCodec -> mapCodec);
	static void bootstrap() {
		ID_MAPPER.put(ResourceLocation.withDefaultNamespace("percentage_base"), PercentageBase.CODEC);
		ID_MAPPER.put(ResourceLocation.withDefaultNamespace("percentage_limit"), PercentageLimit.CODEC);
		ID_MAPPER.put(ResourceLocation.withDefaultNamespace("nullify"), Nullify.CODEC);
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
