package net.atlas.combatify.component.custom;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record CanSweep(boolean enabled) implements TooltipProvider {
	public static final CanSweep DISABLED = new CanSweep(false);
	public static final Codec<CanSweep> CODEC = Codec.BOOL.xmap(CanSweep::new, CanSweep::enabled);
	public static final StreamCodec<ByteBuf, CanSweep> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, CanSweep::enabled, CanSweep::new);
	private static final Component TOOLTIP = Component.translatableWithFallback("item.can_sweep", "Able to Sweep").withStyle(ChatFormatting.BLUE);

	@Override
	public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
		if (this.enabled) consumer.accept(TOOLTIP);
	}
}
