package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.item.CombatifyItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("unused")
@Mixin(PiglinAi.class)
public class PiglinAiMixin {
	@ModifyReturnValue(method = "isWearingSafeArmor", at = @At(value = "RETURN"))
	private static boolean includeHoldingShield(boolean original, @Local(ordinal = 0, argsOnly = true) LivingEntity livingEntity) {
		boolean bl = false;

		for (EquipmentSlot equipmentSlot : EquipmentSlotGroup.HAND) {
			if (livingEntity.getItemBySlot(equipmentSlot).is(CombatifyItemTags.PIGLIN_SAFE_HELD_ITEMS)) {
				bl = true;
				break;
			}
		}

		return original || bl;
	}
}
