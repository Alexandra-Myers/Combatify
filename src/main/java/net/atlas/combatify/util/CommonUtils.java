package net.atlas.combatify.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
//? >1.21.1 {
/*import net.minecraft.util.ExtraCodecs;
*///?}

public class CommonUtils {
	public static final Codec<Float> NON_NEGATIVE_FLOAT =
		//? >1.21.1 {
		/*ExtraCodecs.NON_NEGATIVE_FLOAT;
		*///?} <=1.21.1 {
		Codec.floatRange(0, Float.MAX_VALUE);
		//?}

	public static <T> StreamCodec<ByteBuf, TagKey<T>> tagKeyStreamCodec(ResourceKey<? extends Registry<T>> registryKey) {
		//? >1.21.1 {
		/*return TagKey.streamCodec(registryKey);
		*///?} <=1.21.1 {
		return IDUtils.STREAM_CODEC.map(identifier -> TagKey.create(registryKey, identifier), TagKey::location);
		//?}
	}

	public static Codec<Float> floatRange(float min, float max) {
		return //? >1.21.1 {
			/*ExtraCodecs.floatRange(min, max);
			*///?} <=1.21.1 {
			Codec.floatRange(min, max);
			//?}
	}
}
