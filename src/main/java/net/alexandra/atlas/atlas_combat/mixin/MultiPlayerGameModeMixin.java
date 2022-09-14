package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@ModifyConstant(
			method = "getPickRange",
			require = 2, allow = 2, constant = { @Constant(floatValue = 5.0F), @Constant(floatValue = 4.5F) })
	private float getActualReachDistance(final float reachDistance) {
		if (minecraft.player != null) {
			return (float) ((PlayerExtensions)minecraft.player).getAttackRange(minecraft.player, 4.5F);
		}
		return 4.5F;
	}
	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public boolean hasFarPickRange() {
		return false;
	}

}
