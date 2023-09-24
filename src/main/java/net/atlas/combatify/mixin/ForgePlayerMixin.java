package net.atlas.combatify.mixin;

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

	/**
	 * @author Alexandra
	 * @reason IForgePlayer is an interface, does not support injects
	 */
	@Overwrite(remap = false)
	default double getEntityReach() {
		return MethodHandler.getCurrentAttackReach(self(),0.0F);
	}
}
