package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemProperties.class)
public class ItemPropertiesMixin {
	@WrapMethod(method = "register")
	private static void alterAllBlocking(Item item, ResourceLocation resourceLocation, ClampedItemPropertyFunction clampedItemPropertyFunction, Operation<Void> original) {
		if (resourceLocation.equals(new ResourceLocation("blocking"))) original.call(item, resourceLocation, (ClampedItemPropertyFunction) (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isBlocking() && MethodHandler.getBlockingItem(livingEntity).stack() == itemStack ? 1.0F : 0.0F);
		else original.call(item, resourceLocation, clampedItemPropertyFunction);
	}
}
