package net.atlas.combatify.util.blocking.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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

import java.util.Map;

public record RequiresEmptyHand(InteractionHand interactionHand) implements BlockingCondition {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("requires_empty_hand");
	public static final MapCodec<RequiresEmptyHand> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(Codec.STRING
				.validate(s -> s.equals("off_hand") || s.equals("main_hand") ? DataResult.success(s) : DataResult.error(() -> "Not a valid interaction hand! Input: " + s)).fieldOf("hand")
				.xmap(s -> s.equals("off_hand") ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, interactionHand1 -> interactionHand1 == InteractionHand.MAIN_HAND ? "main_hand" : "off_hand")
				.forGetter(RequiresEmptyHand::interactionHand))
			.apply(instance, RequiresEmptyHand::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, RequiresEmptyHand> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8.map(s -> s.equals("off_hand") ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, interactionHand1 -> interactionHand1 == InteractionHand.MAIN_HAND ? "main_hand" : "off_hand"), RequiresEmptyHand::interactionHand, RequiresEmptyHand::new);

	@Override
	public boolean canBlock(ServerLevel serverLevel, LivingEntity instance, ItemStack blockingItem, DamageSource source, float amount) {
		return instance.getItemInHand(interactionHand).isEmpty();
	}

	@Override
	public boolean canUse(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand) {
		ItemStack oppositeStack = player.getItemInHand(interactionHand());
		return interactionHand != interactionHand() && oppositeStack.isEmpty();
	}

	@Override
	public boolean canShowInToolTip(ItemStack itemStack, Player player) {
		return true;
	}

	@Override
	public boolean overridesUseDurationAndAnimation(ItemStack itemStack) {
		return true;
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
		map.put(ID, STREAM_CODEC.map(requiresEmptyHand -> requiresEmptyHand, blockingCondition -> (RequiresEmptyHand) blockingCondition));
	}
}
