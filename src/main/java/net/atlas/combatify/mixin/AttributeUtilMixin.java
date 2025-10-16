package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.util.AttributeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AttributeUtil.class)
public class AttributeUtilMixin {
	@WrapOperation(method = "applyTextFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;equals(Ljava/lang/Object;)Z"))
	private static boolean modifyBaseID(ResourceLocation instance, Object resourcelocation, Operation<Boolean> original, @Local(ordinal = 0) Holder<Attribute> holder) {
		boolean res = original.call(instance, resourcelocation);
		if (holder.is(Attributes.ATTACK_SPEED)) return res || original.call(instance, WeaponType.BASE_ATTACK_SPEED_CTS_ID);
		if (holder.is(Attributes.ENTITY_INTERACTION_RANGE)) return res || original.call(instance, WeaponType.BASE_ATTACK_REACH_ID);
		return res;
	}
	@ModifyExpressionValue(method = "applyTextFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeBaseValue(Lnet/minecraft/core/Holder;)D"))
	private static double modifyBaseValue(double original, @Local(ordinal = 0) Holder<Attribute> holder, @Local(ordinal = 0) AttributeUtil.BaseModifier modifier) {
		if (holder.is(Attributes.ATTACK_SPEED) && modifier.base().id().equals(WeaponType.BASE_ATTACK_SPEED_CTS_ID)) return original - 1.5;
		if (holder.is(Attributes.ENTITY_INTERACTION_RANGE) && modifier.base().id().equals(WeaponType.BASE_ATTACK_REACH_ID)) return original + (Combatify.CONFIG.attackReach() ? 0 : 0.5);
		return original;
	}
}
