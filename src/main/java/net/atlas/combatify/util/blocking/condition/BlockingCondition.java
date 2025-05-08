package net.atlas.combatify.util.blocking.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface BlockingCondition {
	StreamCodec<RegistryFriendlyByteBuf, BlockingCondition> STREAM_CODEC = StreamCodec.of((buf, blockingCondition) -> {
		buf.writeResourceLocation(blockingCondition.id());
		BlockingConditions.STREAM_CODEC_MAP.get(blockingCondition.id()).encode(buf, blockingCondition);
	}, buf -> BlockingConditions.STREAM_CODEC_MAP.get(buf.readResourceLocation()).decode(buf));
	boolean canUse(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand);

	boolean canShowInToolTip(ItemStack itemStack, Player player);

	boolean appliesComponentModifier(ItemStack itemStack);

	MapCodec<? extends BlockingCondition> type();

	ResourceLocation id();
}
