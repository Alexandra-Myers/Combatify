package net.atlas.combatify.util.blocking.condition;

import com.mojang.serialization.MapCodec;
import net.atlas.combatify.Combatify;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

public record RequiresSwordBlocking() implements BlockingCondition {
	public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("requires_sword_blocking");
	public static final MapCodec<RequiresSwordBlocking> MAP_CODEC = MapCodec.unit(new RequiresSwordBlocking());
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockingCondition> STREAM_CODEC = StreamCodec.unit(new RequiresSwordBlocking());

	@Override
	public boolean canBlock(ServerLevel serverLevel, LivingEntity instance, ItemStack blockingItem, DamageSource source, float amount) {
		return Combatify.CONFIG.swordBlocking();
	}

	@Override
	public boolean canUse(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand) {
		return Combatify.CONFIG.swordBlocking();
	}

	@Override
	public boolean canShowInToolTip(ItemStack itemStack, Player player) {
		return Combatify.CONFIG.swordBlocking();
	}

	@Override
	public boolean overridesUseDurationAndAnimation(ItemStack itemStack) {
		return Combatify.CONFIG.swordBlocking();
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
		map.put(ID, STREAM_CODEC);
	}
}
