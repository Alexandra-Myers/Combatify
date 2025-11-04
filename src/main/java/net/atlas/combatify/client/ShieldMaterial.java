package net.atlas.combatify.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.Identifier;

public record ShieldMaterial(Material base, Material baseNoPattern) {
	private static final Codec<Material> MATERIAL_CODEC = Identifier.CODEC.xmap(Identifier -> new Material(TextureAtlas.LOCATION_BLOCKS, Identifier), Material::texture);
	public static final MapCodec<ShieldMaterial> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(MATERIAL_CODEC.optionalFieldOf("base", ModelBakery.SHIELD_BASE).forGetter(ShieldMaterial::base),
				MATERIAL_CODEC.optionalFieldOf("no_pattern", ModelBakery.NO_PATTERN_SHIELD).forGetter(ShieldMaterial::baseNoPattern))
			.apply(instance, ShieldMaterial::new));
	public Material choose(boolean hasBanner) {
		return hasBanner ? base : baseNoPattern;
	}
}
