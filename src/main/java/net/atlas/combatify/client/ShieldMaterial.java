package net.atlas.combatify.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

public record ShieldMaterial(SpriteId base, SpriteId baseNoPattern) {
	private static final Codec<SpriteId> SPRITE_ID_CODEC = Identifier.CODEC.xmap(Identifier -> new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier), SpriteId::texture);
	public static final MapCodec<ShieldMaterial> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(SPRITE_ID_CODEC.optionalFieldOf("base", Sheets.SHIELD_BASE).forGetter(ShieldMaterial::base),
				SPRITE_ID_CODEC.optionalFieldOf("no_pattern", Sheets.SHIELD_BASE_NO_PATTERN).forGetter(ShieldMaterial::baseNoPattern))
			.apply(instance, ShieldMaterial::new));
	public SpriteId choose(boolean hasBanner) {
		return hasBanner ? base : baseNoPattern;
	}
}
