package net.atlas.combatify.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class IDUtils {
	public static final Codec<Identifier> CODEC = Identifier.CODEC;
	public static final StreamCodec<ByteBuf, Identifier> STREAM_CODEC = Identifier.STREAM_CODEC;

	public static void writeIdentifier(FriendlyByteBuf buf, Identifier identifier) {
		buf.writeIdentifier(identifier);
	}

	public static Identifier readIdentifier(FriendlyByteBuf buf) {
		return buf.readIdentifier();
	}

	public static Identifier fromNamespaceAndPath(String string, String string2) {
		return Identifier.fromNamespaceAndPath(string, string2);
	}

	public static Identifier parse(String string) {
		return Identifier.parse(string);
	}

	public static Identifier withDefaultNamespace(String string) {
		return Identifier.withDefaultNamespace(string);
	}
}
