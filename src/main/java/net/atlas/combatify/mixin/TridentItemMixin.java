package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.WeaponWithType;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TridentItem.class)
public class TridentItemMixin extends Item implements WeaponWithType {

	public TridentItemMixin(Item.Properties properties) {
		super(properties);
	}

	@ModifyExpressionValue(method = "releaseUsing", at = @At(value = "CONSTANT", args = "floatValue=8.0"))
	public float modifyDamage(float original) {
		float diff = (float) (original - Combatify.CONFIG.thrownTridentDamage());
		return original - diff;
	}

	@Override
	public WeaponType getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.TRIDENT;
	}

	@Override
	public Item self() {
		return this;
	}
}
