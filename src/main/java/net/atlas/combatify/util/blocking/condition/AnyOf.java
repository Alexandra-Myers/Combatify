package net.atlas.combatify.util.blocking.condition;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public record AnyOf(List<BlockingCondition> blockingConditions) implements BlockingCondition {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("any_of");
	public static final MapCodec<AnyOf> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(BlockingConditions.MAP_CODEC.codec().listOf().fieldOf("conditions").forGetter(AnyOf::blockingConditions)).apply(instance, AnyOf::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, AnyOf> STREAM_CODEC = StreamCodec.composite(BlockingCondition.STREAM_CODEC.apply(ByteBufCodecs.list()), AnyOf::blockingConditions, AnyOf::new);

	@Override
	public boolean canBlock(ServerLevel serverLevel, LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		AtomicBoolean ret = new AtomicBoolean(false);
		blockingConditions.forEach(blockingCondition -> ret.set(ret.get() | blockingCondition.canBlock(serverLevel, instance, entity, blockingItem, source, amount, f, g, bl)));
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

