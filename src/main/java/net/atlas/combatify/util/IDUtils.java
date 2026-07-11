package net.atlas.combatify.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class IDUtils {
	public static final Codec<ResourceLocation> CODEC = ResourceLocation.CODEC;
	public static final StreamCodec<ByteBuf, ResourceLocation> STREAM_CODEC = ResourceLocation.STREAM_CODEC;

	public static void writeResourceLocation(FriendlyByteBuf buf, ResourceLocation identifier) {
		buf.writeResourceLocation(identifier);
	}

	public static ResourceLocation readResourceLocation(FriendlyByteBuf buf) {
		return buf.readResourceLocation();
	}

	public static ResourceLocation fromNamespaceAndPath(String string, String string2) {
		return ResourceLocation.fromNamespaceAndPath(string, string2);
	}

	public static ResourceLocation parse(String string) {
		return ResourceLocation.parse(string);
	}

	public static ResourceLocation withDefaultNamespace(String string) {
		return ResourceLocation.withDefaultNamespace(string);
	}

	@Deprecated
	public record PseudoId(ResourceLocation id) {
		public static PseudoId fromNamespaceAndPath(String namespace, String path) {
			return new PseudoId(IDUtils.fromNamespaceAndPath(namespace, path));
		}

		public static PseudoId parse(String id) {
			return new PseudoId(IDUtils.parse(id));
		}

		public static PseudoId withDefaultNamespace(String path) {
			return new PseudoId(IDUtils.withDefaultNamespace(path));
		}
	}
	public static final Codec<PseudoId> PSEUDO_CODEC = IDUtils.CODEC.xmap(PseudoId::new, PseudoId::id);
}
