package net.atlas.combatify.util.blocking.condition;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class BlockingConditions {
	public static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends BlockingCondition>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
	public static final BiMap<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, BlockingCondition>> STREAM_CODEC_MAP = HashBiMap.create();
	public static final MapCodec<BlockingCondition> MAP_CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
		.dispatchMap("condition", BlockingCondition::type, mapCodec -> mapCodec);

	public static void bootstrap() {
		ID_MAPPER.put(ResourceLocation.withDefaultNamespace("unconditional"), Unconditional.MAP_CODEC);
		ID_MAPPER.put(ResourceLocation.withDefaultNamespace("requires_sword_blocking"), RequiresSwordBlocking.MAP_CODEC);
		ID_MAPPER.put(ResourceLocation.withDefaultNamespace("requires_empty_hand"), RequiresEmptyHand.MAP_CODEC);
		ID_MAPPER.put(ResourceLocation.withDefaultNamespace("all_of"), AllOf.MAP_CODEC);
		ID_MAPPER.put(ResourceLocation.withDefaultNamespace("any_of"), AnyOf.MAP_CODEC);
		Unconditional.mapStreamCodec(STREAM_CODEC_MAP);
		RequiresSwordBlocking.mapStreamCodec(STREAM_CODEC_MAP);
		RequiresEmptyHand.mapStreamCodec(STREAM_CODEC_MAP);
		AllOf.mapStreamCodec(STREAM_CODEC_MAP);
		AnyOf.mapStreamCodec(STREAM_CODEC_MAP);
	}
}
