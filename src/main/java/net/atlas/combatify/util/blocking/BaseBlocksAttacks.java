package net.atlas.combatify.util.blocking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record BaseBlocksAttacks(List<DamageReduction> damageReductions, float useSeconds,
								float disableCooldownScale, ItemDamageFunction itemDamage) {
		public static final MapCodec<BaseBlocksAttacks> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(DamageReduction.CODEC.listOf().fieldOf("damage_reductions").forGetter(BaseBlocksAttacks::damageReductions),
					Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("seconds", 3600F).forGetter(BaseBlocksAttacks::useSeconds),
					Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("disable_cooldown_scale", 1.0F).forGetter(BaseBlocksAttacks::disableCooldownScale),
					ItemDamageFunction.CODEC.optionalFieldOf("item_damage", ItemDamageFunction.DEFAULT).forGetter(BaseBlocksAttacks::itemDamage))
				.apply(instance, BaseBlocksAttacks::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, BaseBlocksAttacks> STREAM_CODEC = StreamCodec.composite(
			DamageReduction.STREAM_CODEC.apply(ByteBufCodecs.list()),
			BaseBlocksAttacks::damageReductions,
			ByteBufCodecs.FLOAT,
			BaseBlocksAttacks::useSeconds,
			ByteBufCodecs.FLOAT,
			BaseBlocksAttacks::disableCooldownScale,
			ItemDamageFunction.STREAM_CODEC,
			BaseBlocksAttacks::itemDamage,
			BaseBlocksAttacks::new
		);
	}
