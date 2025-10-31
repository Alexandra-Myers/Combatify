package net.atlas.combatify.client;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

public record ShieldMaterial(Material base, Material baseNoPattern) {
	public static final ShieldMaterial IRON_SHIELD = new ShieldMaterial(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/iron_shield_base")), new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/iron_shield_base_nopattern")));
	public static final ShieldMaterial GOLDEN_SHIELD = new ShieldMaterial(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/golden_shield_base")), new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/golden_shield_base_nopattern")));
	public static final ShieldMaterial DIAMOND_SHIELD = new ShieldMaterial(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/diamond_shield_base")), new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/diamond_shield_base_nopattern")));
	public static final ShieldMaterial NETHERITE_SHIELD = new ShieldMaterial(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/netherite_shield_base")), new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("combatify/netherite_shield_base_nopattern")));
	public Material choose(boolean hasBanner) {
		return hasBanner ? base : baseNoPattern;
	}
}
