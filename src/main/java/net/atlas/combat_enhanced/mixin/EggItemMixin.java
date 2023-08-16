package net.atlas.combat_enhanced.mixin;

import net.atlas.combat_enhanced.CombatEnhanced;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EggItem.class)
public class EggItemMixin {

	@Inject(method = "use", at = @At("RETURN"))
	public void injectDelay(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
		var egg = EggItem.class.cast(this);
		user.getCooldowns().addCooldown(egg, CombatEnhanced.CONFIG.eggItemCooldown());
	}

}
