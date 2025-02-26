package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import static net.atlas.combatify.util.MethodHandler.getFatigueForTime;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends ProjectileWeaponItem {

	private BowItemMixin(Properties properties) {
		super(properties);
	}
	@ModifyExpressionValue(method = "releaseUsing", at = @At(value = "CONSTANT", args = "floatValue=1.0F", ordinal = 0))
	public float releaseUsing(float original, @Local(ordinal = 1) final int time) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return (float) (Combatify.CONFIG.bowUncertainty() * getFatigueForTime(time));
	}
}
