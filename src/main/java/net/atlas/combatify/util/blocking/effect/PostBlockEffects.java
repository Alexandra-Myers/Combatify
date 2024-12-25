package net.atlas.combatify.util.blocking.effect;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class PostBlockEffects {
	public static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends PostBlockEffect>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
	public static final BiMap<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, PostBlockEffect>> STREAM_CODEC_MAP = HashBiMap.create();
	public static final MapCodec<PostBlockEffect> MAP_CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
		.dispatchMap("effect", PostBlockEffect::type, mapCodec -> mapCodec);

	public static void bootstrap() {
		ID_MAPPER.put(DoNothing.ID, DoNothing.MAP_CODEC);
		ID_MAPPER.put(KnockbackAttacker.ID, KnockbackAttacker.MAP_CODEC);
		ID_MAPPER.put(ApplyEffectOnBlocked.ID, ApplyEffectOnBlocked.MAP_CODEC);
		ID_MAPPER.put(RunFunction.ID, RunFunction.MAP_CODEC);
		ID_MAPPER.put(AllOf.ID, AllOf.MAP_CODEC);
		DoNothing.mapStreamCodec(STREAM_CODEC_MAP);
		KnockbackAttacker.mapStreamCodec(STREAM_CODEC_MAP);
		ApplyEffectOnBlocked.mapStreamCodec(STREAM_CODEC_MAP);
		RunFunction.mapStreamCodec(STREAM_CODEC_MAP);
		AllOf.mapStreamCodec(STREAM_CODEC_MAP);
	}
}
