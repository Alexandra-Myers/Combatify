package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.Multimap;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions {

	@Shadow
	public abstract Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot);

	@Override
	public double getAttackReach(Player player) {
		float var2 = 0.0F;
		float var3 = player.getAttackStrengthScale(1.0F);
		if (var3 > 1.95F && !player.isCrouching()) {
			var2 = 1.0F;
		}
		return 2.5 + var2;
	}

	@Override
	public double getAttackSpeed(Player player) {
		return 4.0;
	}
	@Override
	public double getAttackDamage(Player player) {
		return 1.0;
	}
	@Override
	public void setStackSize(int stackSize) {
		((Item) (Object)this).maxStackSize = stackSize;
	}

	/**
	 * @author zOnlyKroks
	 * @reason doesnt hurt other mods
	 */
	@Overwrite
	public int getUseDuration(ItemStack stack) {
		if (stack.getItem() instanceof BowlFoodItem) {
			return 20;
		}else if (stack.getItem().isEdible()) {
			return ((Item) (Object)this).getFoodProperties().isFastFood() ? 16 : 32;
		} else {
			return 0;
		}
	}

}
