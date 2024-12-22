package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.Tier;
import net.minecraft.world.item.ToolMaterial;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ToolMaterial.class)
public abstract class ToolMaterialMixin implements Tier {
	@Shadow
	public abstract boolean equals(Object par1);

	@Shadow
	@Final
	public static ToolMaterial NETHERITE;

	@Shadow
	@Final
	public static ToolMaterial DIAMOND;

	@Shadow
	@Final
	public static ToolMaterial IRON;

	@Shadow
	@Final
	public static ToolMaterial STONE;

	@Unique
	public final ToolMaterial thisMaterial = ToolMaterial.class.cast(this);

	@Override
	public int level() {
		if (thisMaterial.equals(NETHERITE)) return 4;
		if (thisMaterial.equals(DIAMOND)) return 3;
		if (thisMaterial.equals(IRON)) return 2;
		if (thisMaterial.equals(STONE)) return 1;
		return 0;
	}

	@Override
	public ToolMaterial asMaterial() {
		return thisMaterial;
	}
}
