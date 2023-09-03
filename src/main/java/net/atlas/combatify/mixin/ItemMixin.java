package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.ItemExtensions;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions {

	@Override
	public void setStackSize(int stackSize) {
		((Item) (Object)this).maxStackSize = stackSize;
	}

	@Inject(method = "getUseDuration", at = @At(value = "RETURN"), cancellable = true)
	public void getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (stack.getItem() instanceof BowlFoodItem || stack.getItem() instanceof SuspiciousStewItem) {
			cir.setReturnValue(Combatify.CONFIG.stewUseDuration());
		} else if (stack.getItem().isEdible()) {
			cir.setReturnValue(Objects.requireNonNull(((Item) (Object) this).getFoodProperties()).isFastFood() ? 16 : 32);
		} else {
			cir.setReturnValue(0);
		}
	}

}
