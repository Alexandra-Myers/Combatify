package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	@Final
	public Options options;

	@Shadow
	protected int missTime;

	@Shadow
	protected abstract boolean startAttack();

	@Shadow
	@Nullable
	public LocalPlayer player;

	@Shadow
	protected abstract void continueAttack(boolean b);

	@Shadow
	@Nullable
	public HitResult hitResult;
	@Shadow
	private int rightClickDelay;
	@Shadow
	@Final
	private static Logger LOGGER;

	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"))
	public void redirectContinueAttack(Minecraft instance, boolean b) {
		if(hitResult.getType() == HitResult.Type.BLOCK) {
			continueAttack(b);
		}else {
			if (b && ((IOptions) options).autoAttack().get()) {
				if (player.getAttackStrengthScale(0.5F) > 0.5F) {
					startAttack();
				}
			} else {
				continueAttack(b);
			}
		}
	}
	@ModifyConstant(method = "startAttack", constant = @Constant(intValue = 10))
	public int redirectMissPenalty(int constant) {
		return 4;
	}
}
