package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.Tier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ToolMaterial.class)
public abstract class ToolMaterialMixin implements Tier {

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
	@Shadow
	public abstract TagKey<Block> incorrectBlocksForDrops();

	@Override
	@Shadow
	public abstract int durability();

	@Override
	@Shadow
	public abstract float speed();

	@Override
	@Shadow
	public abstract float attackDamageBonus();

	@Override
	@Shadow
	public abstract int enchantmentValue();

	@Override
	@Shadow
	public abstract TagKey<Item> repairItems();

	@Override
	public int combatify$level() {
		if (thisMaterial.equals(NETHERITE)) return 4;
		if (thisMaterial.equals(DIAMOND)) return 3;
		if (thisMaterial.equals(IRON)) return 2;
		if (thisMaterial.equals(STONE)) return 1;
		return 0;
	}

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ToolMaterial;<init>(Lnet/minecraft/tags/TagKey;IFFILnet/minecraft/tags/TagKey;)V"), index = 3)
	private static float modifyDamage(float f) {
		if (f > 0 && Combatify.CONFIG.ctsAttackBalancing()) {
			f -= 1;
		}
		return f;
	}
}
