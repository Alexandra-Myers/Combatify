package net.atlas.combatify.util.blocking.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public record AnyOf(List<BlockingCondition> blockingConditions) implements BlockingCondition {
	public AnyOf(BlockingCondition... conditions) {
		this(Arrays.asList(conditions));
	}
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("any_of");
	public static final MapCodec<AnyOf> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(BlockingConditions.MAP_CODEC.codec().listOf().fieldOf("conditions").forGetter(AnyOf::blockingConditions)).apply(instance, AnyOf::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, AnyOf> STREAM_CODEC = StreamCodec.composite(BlockingCondition.STREAM_CODEC.apply(ByteBufCodecs.list()), AnyOf::blockingConditions, AnyOf::new);

	@Override
	public boolean canBlock(ServerLevel serverLevel, LivingEntity instance, ItemStack blockingItem, DamageSource source, float amount) {
		AtomicBoolean ret = new AtomicBoolean(false);
		blockingConditions.forEach(blockingCondition -> ret.set(ret.get() | blockingCondition.canBlock(serverLevel, instance, blockingItem, source, amount)));
		return ret.get();
	}

	@Override
	public boolean canUse(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand) {
		AtomicBoolean ret = new AtomicBoolean(false);
		blockingConditions.forEach(blockingCondition -> ret.set(ret.get() | blockingCondition.canUse(itemStack, level, player, interactionHand)));
		return ret.get();
	}

	@Override
	public boolean canShowInToolTip(ItemStack itemStack, Player player) {
		AtomicBoolean ret = new AtomicBoolean(false);
		blockingConditions.forEach(blockingCondition -> ret.set(ret.get() | blockingCondition.canShowInToolTip(itemStack, player)));
		return ret.get();
	}

	@Override
	public boolean overridesUseDurationAndAnimation(ItemStack itemStack) {
		AtomicBoolean ret = new AtomicBoolean(false);
		blockingConditions.forEach(blockingCondition -> ret.set(ret.get() | blockingCondition.overridesUseDurationAndAnimation(itemStack)));
		return ret.get();
	}

	@Override
	public boolean appliesComponentModifier(ItemStack itemStack) {
		AtomicBoolean ret = new AtomicBoolean(false);
		blockingConditions.forEach(blockingCondition -> ret.set(ret.get() | blockingCondition.appliesComponentModifier(itemStack)));
		return ret.get();
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
		map.put(ID, STREAM_CODEC.map(anyOf -> anyOf, blockingCondition -> (AnyOf) blockingCondition));
	}
}

