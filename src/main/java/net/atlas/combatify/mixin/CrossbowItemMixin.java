package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.Combatify;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends ProjectileWeaponItem {

	private CrossbowItemMixin(Properties properties) {
		super(properties);
	}
	@ModifyExpressionValue(method = "use", at = @At(value = "CONSTANT", args = "floatValue=1.0F", ordinal = 0))
	public float releaseUsing(float original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return Combatify.CONFIG.crossbowUncertainty().floatValue();
	}
}
