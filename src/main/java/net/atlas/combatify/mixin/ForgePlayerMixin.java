package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.extensions.IForgePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IForgePlayer.class)
public interface ForgePlayerMixin {
	@Shadow
	Player self();

	@WrapMethod(method = "getEntityReach")
	default double getEntityReach(Operation<Double> original) {
		return MethodHandler.getCurrentAttackReach(self(),0.0F);
	}
}
