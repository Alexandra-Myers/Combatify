package net.atlas.combatify.util.blocking.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;

public record Unconditional() implements BlockingCondition {
	public static final Identifier ID = Identifier.withDefaultNamespace("unconditional");
	public static final Unconditional INSTANCE = new Unconditional();
	public static final MapCodec<Unconditional> MAP_CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockingCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public boolean canUse(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand) {
		return true;
	}

	@Override
	public boolean canShowInToolTip(ItemStack itemStack, Player player) {
		return true;
	}

    @Override
	public boolean appliesComponentModifier(ItemStack itemStack) {
		return true;
	}

	@Override
	public MapCodec<? extends BlockingCondition> type() {
		return MAP_CODEC;
	}

	@Override
	public Identifier id() {
		return ID;
	}

	public static void mapStreamCodec(Map<Identifier, StreamCodec<RegistryFriendlyByteBuf, BlockingCondition>> map) {
		map.put(ID, STREAM_CODEC);
	}
}
