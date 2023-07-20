package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.minecraft.world.entity.player.Player;
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
		return 2.0;
	}
	@Override
	public void setStackSize(int stackSize) {
		((Item) (Object)this).maxStackSize = stackSize;
	}

	@Inject(method = "getUseDuration", at = @At(value = "RETURN"), cancellable = true)
	public void getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (stack.getItem() instanceof BowlFoodItem || stack.getItem() instanceof SuspiciousStewItem) {
			cir.setReturnValue(AtlasCombat.CONFIG.stewUseDuration());
		}else if (stack.getItem().isEdible()) {
			cir.setReturnValue(Objects.requireNonNull(((Item) (Object) this).getFoodProperties()).isFastFood() ? 16 : 32);
		} else {
			cir.setReturnValue(0);
		}
	}

}
