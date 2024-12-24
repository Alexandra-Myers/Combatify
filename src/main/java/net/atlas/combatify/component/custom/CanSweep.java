package net.atlas.combatify.component.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record CanSweep(boolean enabled, boolean showInTooltip) implements TooltipProvider {
	public static final CanSweep DISABLED = new CanSweep(false, false);
	public static final Codec<CanSweep> FULL_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.BOOL.optionalFieldOf("enabled", Boolean.TRUE).forGetter(CanSweep::enabled),
				Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.FALSE).forGetter(CanSweep::showInTooltip))
			.apply(instance, CanSweep::new)
	);
	public static final Codec<CanSweep> CODEC = Codec.withAlternative(FULL_CODEC, Codec.BOOL.xmap(enabled -> new CanSweep(enabled, false), CanSweep::enabled));
	public static final StreamCodec<ByteBuf, CanSweep> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, CanSweep::enabled, ByteBufCodecs.BOOL, CanSweep::showInTooltip, CanSweep::new);
	private static final Component TOOLTIP = Component.translatable("item.can_sweep").withStyle(ChatFormatting.BLUE);

	@Override
	public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		if (this.showInTooltip && this.enabled) {
			consumer.accept(TOOLTIP);
		}
	}
}
