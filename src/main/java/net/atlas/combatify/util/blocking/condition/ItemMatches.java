package net.atlas.combatify.util.blocking.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;

public record ItemMatches(ItemPredicate predicate, boolean invert) implements BlockingCondition {
	public static final Codec<ItemPredicate> ITEM_PREDICATE_CODEC_NO_ITEMS = RecordCodecBuilder.create(
		instance -> instance.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::count),
				DataComponentMatchers.CODEC.forGetter(ItemPredicate::components)
			)
			.apply(instance, (ints, dataComponentMatchers) -> new ItemPredicate(Optional.empty(), ints, dataComponentMatchers))
	);
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("item_matches");
	public static final MapCodec<ItemMatches> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(ITEM_PREDICATE_CODEC_NO_ITEMS.fieldOf("predicate").forGetter(ItemMatches::predicate),
				Codec.BOOL.optionalFieldOf("invert", false).forGetter(ItemMatches::invert))
			.apply(instance, ItemMatches::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemMatches> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.fromCodecTrusted(ITEM_PREDICATE_CODEC_NO_ITEMS), ItemMatches::predicate,
		ByteBufCodecs.BOOL, ItemMatches::invert,
		ItemMatches::new);

	@Override
	public boolean canUse(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand) {
		return predicate.test(itemStack) != invert;
	}

	@Override
	public boolean canShowInToolTip(ItemStack itemStack, Player player) {
		return predicate.test(itemStack) != invert;
	}

    @Override
	public boolean appliesComponentModifier(ItemStack itemStack) {
		return predicate.test(itemStack) != invert;
	}

	@Override
	public MapCodec<? extends BlockingCondition> type() {
		return MAP_CODEC;
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static void mapStreamCodec(Map<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, BlockingCondition>> map) {
		map.put(ID, STREAM_CODEC.map(itemMatches -> itemMatches, blockingCondition -> (ItemMatches) blockingCondition));
	}
}
