package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin {
	@WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;has(Lnet/minecraft/core/component/DataComponentType;)Z"))
	public boolean ignoreComponentIfUnableToBlock(ItemStack instance, DataComponentType<?> dataComponentType, Operation<Boolean> original, @Local(ordinal = 0, argsOnly = true) Level level, @Local(ordinal = 0, argsOnly = true) Player player, @Local(ordinal = 0, argsOnly = true) InteractionHand interactionHand) {
		if (!MethodHandler.getBlocking(instance).canUse(instance, level, player, interactionHand)) return false;
		return original.call(instance, dataComponentType);
	}
}
