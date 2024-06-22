package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends ProjectileWeaponItem {

	private CrossbowItemMixin(Properties properties) {
		super(properties);
	}
	@ModifyConstant(method = "use", constant = @Constant(floatValue = 1.0F, ordinal = 0))
	public float releaseUsing(float constant) {
		return Combatify.CONFIG.crossbowUncertainty().floatValue();
	}
}
