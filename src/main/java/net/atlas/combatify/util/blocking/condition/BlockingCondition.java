package net.atlas.combatify.util.blocking.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public interface BlockingCondition {
	StreamCodec<@NonNull RegistryFriendlyByteBuf, @NonNull BlockingCondition> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(BlockingConditions.MAP_CODEC.codec());
	boolean canUse(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand);

	boolean canShowInToolTip(ItemStack itemStack, Player player);

	boolean appliesComponentModifier(ItemStack itemStack);

	MapCodec<? extends BlockingCondition> type();
}
