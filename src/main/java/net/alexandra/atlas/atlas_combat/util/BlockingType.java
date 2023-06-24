package net.alexandra.atlas.atlas_combat.util;

import net.minecraft.resources.ResourceLocation;

public class BlockingType {
	private final ResourceLocation name;
	public static final BlockingType SWORD = new BlockingType("sword");
	public static final BlockingType SHIELD = new BlockingType("shield");
	protected BlockingType(String name) {
		this.name = new ResourceLocation("atlas_combat", name);
	}

	public ResourceLocation getName() {
		return name;
	}
}
