package net.atlas.combat_enhanced.mixin;

import net.atlas.combat_enhanced.item.WeaponType;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShovelItem.class)
public class ShovelItemMixin extends DiggerItemMixin {
	public ShovelItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public WeaponType getWeaponType() {
		return WeaponType.SHOVEL;
	}

}
