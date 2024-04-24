package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.CombatifyItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.BreachEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BreachEnchantment.class)
public class BreachEnchantmentMixin {
	@ModifyExpressionValue(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/tags/ItemTags;MACE_ENCHANTABLE:Lnet/minecraft/tags/TagKey;"))
	private static TagKey<Item> changeTag(TagKey<Item> original) {
		return CombatifyItemTags.BREACH_ENCHANTABLE;
	}
	@ModifyExpressionValue(method = "calculateArmorBreach", at = @At(value = "CONSTANT", args = "floatValue=0.15"))
	private static float modifyStrength(float original) {
		return Combatify.CONFIG.breachArmorPiercing().floatValue();
	}
}
