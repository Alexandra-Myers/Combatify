package net.atlas.combatify.mixin.compatibility.neoforge;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class ModifyShieldMixin {
	@SuppressWarnings({"MixinAnnotationTarget", "InvalidInjectorMethodSignature"})
	@ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/extensions/IItemStackExtension;canPerformAction(Lnet/neoforged/neoforge/common/ItemAbility;)Z"))
	public boolean changeUsedShield(boolean original) {
		return !MethodHandler.getBlockingItem(Player.class.cast(this)).stack().isEmpty() || original;
	}
}
