package net.atlas.combatify.util;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DualMapCodec<B extends ByteBuf, T>(MapCodec<T> mapCodec, StreamCodec<B, T> streamCodec) {
}
