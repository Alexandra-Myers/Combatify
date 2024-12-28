package net.atlas.combatify.util.blocking.damage_parsers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.atlas.combatify.util.blocking.BlockingTypeInit;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.LootContext;

public interface DamageParser {
	Codec<DamageParser> CODEC = BlockingTypeInit.DAMAGE_PARSER_TYPE_REG
		.byNameCodec()
		.dispatch(DamageParser::codec, mapCodec -> mapCodec);
	static void bootstrap(Registry<MapCodec<? extends DamageParser>> registry) {
		Registry.register(registry, "direct", Direct.CODEC);
		Registry.register(registry, "nullify", Nullify.CODEC);
		Registry.register(registry, "percentage", Percentage.CODEC);
		Registry.register(registry, "percentage_limit", PercentageLimit.CODEC);
	}
	float parse(float originalValue, float protection, LootContext context);

	MapCodec<? extends DamageParser> codec();
}
