package net.atlas.combatify.util.blocking.condition;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.extension.LateBoundIdMapper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class BlockingConditions {
	public static final LateBoundIdMapper<ResourceLocation, MapCodec<? extends BlockingCondition>> ID_MAPPER = new LateBoundIdMapper<>();
	public static final BiMap<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, BlockingCondition>> STREAM_CODEC_MAP = HashBiMap.create();
	public static final MapCodec<BlockingCondition> MAP_CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
		.dispatchMap("condition", BlockingCondition::type, mapCodec -> mapCodec);

	public static void bootstrap() {
		ID_MAPPER.put(Unconditional.ID, Unconditional.MAP_CODEC);
		ID_MAPPER.put(RequiresEmptyHand.ID, RequiresEmptyHand.MAP_CODEC);
		ID_MAPPER.put(ItemMatches.ID, ItemMatches.MAP_CODEC);
		ID_MAPPER.put(AllOf.ID, AllOf.MAP_CODEC);
		ID_MAPPER.put(AnyOf.ID, AnyOf.MAP_CODEC);
		Unconditional.mapStreamCodec(STREAM_CODEC_MAP);
		RequiresEmptyHand.mapStreamCodec(STREAM_CODEC_MAP);
		ItemMatches.mapStreamCodec(STREAM_CODEC_MAP);
		AllOf.mapStreamCodec(STREAM_CODEC_MAP);
		AnyOf.mapStreamCodec(STREAM_CODEC_MAP);
	}
}
