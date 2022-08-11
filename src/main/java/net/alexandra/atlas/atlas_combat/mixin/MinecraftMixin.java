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
	@Inject(method = "startUseItem", at = @At(value = "HEAD"), cancellable = true)
	public void injectShieldFunctions(CallbackInfo ci) {
		if(player.getOffhandItem().getItem() instanceof ShieldItem) {
			InteractionHand interactionHand = InteractionHand.OFF_HAND;
			if (!((Minecraft) (Object) this).gameMode.isDestroying()) {
				rightClickDelay = 4;
				if (!this.player.isHandsBusy()) {
					if (this.hitResult == null) {
						LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
					}
						ItemStack itemStack = this.player.getItemInHand(interactionHand);
						if (this.hitResult != null) {
							switch(this.hitResult.getType()) {
								case ENTITY:
									EntityHitResult entityHitResult = (EntityHitResult)this.hitResult;
									Entity entity = entityHitResult.getEntity();
									if (!((Minecraft) (Object) this).level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
										return;
									}

									InteractionResult interactionResult = ((Minecraft) (Object) this).gameMode.interactAt(this.player, entity, entityHitResult, interactionHand);
									if (!interactionResult.consumesAction()) {
										interactionResult = ((Minecraft) (Object) this).gameMode.interact(this.player, entity, interactionHand);
									}

									if (interactionResult.consumesAction()) {
										if (interactionResult.shouldSwing()) {
											this.player.swing(interactionHand);
										}

										return;
									}
									break;
								case BLOCK:
									BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
									int i = itemStack.getCount();
									InteractionResult interactionResult2 = ((Minecraft) (Object) this).gameMode.useItemOn(((Minecraft) (Object) this).player, interactionHand, blockHitResult);
									if (interactionResult2.consumesAction()) {
										if (interactionResult2.shouldSwing()) {
											this.player.swing(interactionHand);
											if (!itemStack.isEmpty() && (itemStack.getCount() != i || ((Minecraft) (Object) this).gameMode.hasInfiniteItems())) {
												((Minecraft) (Object) this).gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
											}
										}

										return;
									}

									if (interactionResult2 == InteractionResult.FAIL) {
										return;
									}
							}
						}

						if (!itemStack.isEmpty()) {
							InteractionResult interactionResult3 = ((Minecraft) (Object) this).gameMode.useItem(this.player, interactionHand);
							if (interactionResult3.consumesAction()) {
								if (interactionResult3.shouldSwing()) {
									this.player.swing(interactionHand);
								}

								((Minecraft) (Object) this).gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
								return;
							}
						}
					ci.cancel();
				}
			}
		}else if(player.getMainHandItem().getItem() instanceof ShieldItem) {
			InteractionHand interactionHand = InteractionHand.MAIN_HAND;
			if (!((Minecraft) (Object) this).gameMode.isDestroying()) {
				rightClickDelay = 4;
				if (!this.player.isHandsBusy()) {
					if (this.hitResult == null) {
						LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
					}
					ItemStack itemStack = this.player.getItemInHand(interactionHand);
					if (this.hitResult != null) {
						switch(this.hitResult.getType()) {
							case ENTITY:
								EntityHitResult entityHitResult = (EntityHitResult)this.hitResult;
								Entity entity = entityHitResult.getEntity();
								if (!((Minecraft) (Object) this).level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
									return;
								}

								InteractionResult interactionResult = ((Minecraft) (Object) this).gameMode.interactAt(this.player, entity, entityHitResult, interactionHand);
								if (!interactionResult.consumesAction()) {
									interactionResult = ((Minecraft) (Object) this).gameMode.interact(this.player, entity, interactionHand);
								}

								if (interactionResult.consumesAction()) {
									if (interactionResult.shouldSwing()) {
										this.player.swing(interactionHand);
									}

									return;
								}
								break;
							case BLOCK:
								BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
								int i = itemStack.getCount();
								InteractionResult interactionResult2 = ((Minecraft) (Object) this).gameMode.useItemOn(((Minecraft) (Object) this).player, interactionHand, blockHitResult);
								if (interactionResult2.consumesAction()) {
									if (interactionResult2.shouldSwing()) {
										this.player.swing(interactionHand);
										if (!itemStack.isEmpty() && (itemStack.getCount() != i || ((Minecraft) (Object) this).gameMode.hasInfiniteItems())) {
											((Minecraft) (Object) this).gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
										}
									}

									return;
								}

								if (interactionResult2 == InteractionResult.FAIL) {
									return;
								}
						}
					}

					if (!itemStack.isEmpty()) {
						InteractionResult interactionResult3 = ((Minecraft) (Object) this).gameMode.useItem(this.player, interactionHand);
						if (interactionResult3.consumesAction()) {
							if (interactionResult3.shouldSwing()) {
								this.player.swing(interactionHand);
							}

							((Minecraft) (Object) this).gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
							return;
						}
					}
					ci.cancel();
				}
			}
		}
	}
	@ModifyConstant(method = "startAttack", constant = @Constant(intValue = 10))
	public int redirectMissPenalty(int constant) {
		return 4;
	}
}
