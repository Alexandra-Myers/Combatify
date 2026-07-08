package net.atlas.combatify.util.blocking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public record ItemDamageFunction(float threshold, float base, float factor) {
	public static final Codec<ItemDamageFunction> CODEC = RecordCodecBuilder.create(
		i -> i.group(Codec.floatRange(0, Float.MAX_VALUE).fieldOf("threshold").forGetter(ItemDamageFunction::threshold),
			 Codec.FLOAT.fieldOf("base").forGetter(ItemDamageFunction::base),
			 Codec.FLOAT.fieldOf("factor").forGetter(ItemDamageFunction::factor))
		 .apply(i, ItemDamageFunction::new)
	);
	public static final StreamCodec<ByteBuf, ItemDamageFunction> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT,
		ItemDamageFunction::threshold,
		ByteBufCodecs.FLOAT,
		ItemDamageFunction::base,
		ByteBufCodecs.FLOAT,
		ItemDamageFunction::factor,
		ItemDamageFunction::new
	);
	public static final ItemDamageFunction DEFAULT = new ItemDamageFunction(1.0F, 0.0F, 1.0F);

	public int apply(final float dealtDamage) {
		return dealtDamage < this.threshold ? 0 : Mth.floor(this.base + this.factor * dealtDamage);
	}
}
