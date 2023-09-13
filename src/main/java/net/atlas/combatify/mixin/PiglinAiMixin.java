package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.item.TieredShieldItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("unused")
@Mixin(PiglinAi.class)
public class PiglinAiMixin {
	@ModifyReturnValue(method = "isWearingGold", at = @At(value = "RETURN"))
	private static boolean includeHoldingShield(boolean original, @Local(ordinal = 0) LivingEntity livingEntity) {
		boolean bl = false;
		Iterable<ItemStack> iterable = livingEntity.getHandSlots();

		for (ItemStack itemStack : iterable) {
			Item item = itemStack.getItem();
			if(item instanceof TieredShieldItem tieredShield && tieredShield.getTier() == Tiers.GOLD) {
				bl = true;
			}
		}

		return original || bl;
	}
}
