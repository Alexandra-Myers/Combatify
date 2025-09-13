package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IsUsingItem.class)
public class IsUsingItemMixin {
	@WrapMethod(method = "get")
	public boolean accountForBlocking(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext, Operation<Boolean> original) {
		if (livingEntity != null && livingEntity.isBlocking() && MethodHandler.getBlockingItem(livingEntity).stack() == itemStack) return true;
		return original.call(itemStack, clientLevel, livingEntity, i, itemDisplayContext);
	}
}
