package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.WeaponWithType;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MaceItem.class)
public class MaceItemMixin extends Item implements WeaponWithType {

	public MaceItemMixin(Properties properties) {
		super(properties);
	}

	@Override
	public WeaponType getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(this).type;
			if (type != null)
				return type;
		}
		return WeaponType.MACE;
	}

	@Override
	public Item self() {
		return this;
	}
}
