package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.Combatify;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TridentItem.class)
public class TridentItemMixin extends Item {

	public TridentItemMixin(Item.Properties properties) {
		super(properties);
	}

	@ModifyExpressionValue(method = "releaseUsing", at = @At(value = "CONSTANT", args = "floatValue=8.0"))
	public float modifyDamage(float original) {
		float diff = (float) (original - Combatify.CONFIG.thrownTridentDamage());
		return original - diff;
	}
}
