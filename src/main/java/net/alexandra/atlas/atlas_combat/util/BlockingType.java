package net.alexandra.atlas.atlas_combat.util;

import net.minecraft.resources.ResourceLocation;

public class BlockingType {
	private final ResourceLocation name;
	public static final BlockingType SWORD = new BlockingType("sword").setToolBlocker(true).setDisablement(false);
	public static final BlockingType SHIELD = new BlockingType("shield").setCrouchable(true).setBlockHit(false);
	private boolean canBeDisabled = true;
	private boolean canCrouchBlock = false;
	private boolean isToolBlocker = false;
	private boolean canBlockHit = true;
	public boolean canCrouchBlock() {
		return canCrouchBlock;
	}
	public BlockingType setCrouchable(boolean crouchable) {
		canCrouchBlock = crouchable;
		return this;
	}

	public boolean canBlockHit() {
		return canBlockHit;
	}
	public BlockingType setBlockHit(boolean blockHit) {
		canBlockHit = blockHit;
		return this;
	}
	public boolean isToolBlocker() {
		return isToolBlocker;
	}
	public BlockingType setToolBlocker(boolean isTool) {
		isToolBlocker = isTool;
		return this;
	}
	public boolean canBeDisabled() {
		return canBeDisabled;
	}
	public BlockingType setDisablement(boolean canDisable) {
		canBeDisabled = canDisable;
		return this;
	}

	protected BlockingType(String name) {
		this.name = new ResourceLocation("atlas_combat", name);
	}

	public ResourceLocation getName() {
		return name;
	}
}
