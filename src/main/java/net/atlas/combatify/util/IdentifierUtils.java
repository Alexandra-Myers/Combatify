package net.atlas.combatify.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;
//? >=1.21.11 {
/*import net.minecraft.resources.Identifier;
	*///?} <1.21.11 {
import net.minecraft.resources.ResourceLocation;
//?}

public class IdentifierUtils {
	//? >=1.21.11 {
	/*public static final Codec<Identifier> CODEC = Identifier.CODEC;
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

	@Nullable
	public static Identifier tryParse(String string) {
		return Identifier.tryParse(string);
	}

	public record PseudoId(Identifier id) {
	*///?} <1.21.11 {
	public static final Codec<ResourceLocation> CODEC = ResourceLocation.CODEC;
	public static final StreamCodec<ByteBuf, ResourceLocation> STREAM_CODEC = ResourceLocation.STREAM_CODEC;

	public static void writeIdentifier(FriendlyByteBuf buf, ResourceLocation identifier) {
		buf.writeResourceLocation(identifier);
	}

	public static ResourceLocation readIdentifier(FriendlyByteBuf buf) {
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
	public record PseudoId(ResourceLocation id) {
	//?}
		public static PseudoId fromNamespaceAndPath(String namespace, String path) {
			return new PseudoId(IdentifierUtils.fromNamespaceAndPath(namespace, path));
		}

		public static PseudoId parse(String id) {
			return new PseudoId(IdentifierUtils.parse(id));
		}

		public static PseudoId withDefaultNamespace(String path) {
			return new PseudoId(IdentifierUtils.withDefaultNamespace(path));
		}
	}
	public static final Codec<PseudoId> PSEUDO_CODEC = IdentifierUtils.CODEC.xmap(PseudoId::new, PseudoId::id);
}
