package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.TieredShieldItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
	@Mutable
	@Shadow
	@Final
	ItemStack result;

	@Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/world/item/crafting/CraftingBookCategory;Lnet/minecraft/world/item/crafting/ShapedRecipePattern;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "RETURN"))
	public void changeResult(String string, CraftingBookCategory craftingBookCategory, ShapedRecipePattern shapedRecipePattern, ItemStack itemStack, CallbackInfo ci) {
		if(Combatify.CONFIG.tieredShields() && itemStack.is(Items.SHIELD)) {
			ItemStack stack = new ItemStack(TieredShieldItem.WOODEN_SHIELD, itemStack.getCount());
			stack.setPopTime(itemStack.getPopTime());
			if(itemStack.getTag() != null)
				stack.setTag(itemStack.getTag().copy());
			result = stack;
		}
	}
}
