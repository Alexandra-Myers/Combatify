package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.IAxeItem;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxeItem.class)
public class AxeItemMixin extends DiggerItemMixin implements IAxeItem {
	public AxeItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}
	@Override
	public float getShieldCooldownMultiplier(int shieldDisable) {
		return 1.6F + shieldDisable * 0.5F;
	}

	@Override
	public WeaponType getWeaponType() {
		return WeaponType.AXE;
	}
}
