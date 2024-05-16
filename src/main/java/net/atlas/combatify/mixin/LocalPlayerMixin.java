package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements PlayerExtensions, LivingEntityExtensions {
	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Shadow
	public abstract void startUsingItem(InteractionHand interactionHand);

	@Shadow
	private boolean startedUsingItem;


	@Unique
	BooleanOption force100PercentRecharge;

	@Shadow
	@Final
	public ClientPacketListener connection;
	@Unique
	@Final
	public Minecraft minecraft = Minecraft.getInstance();
	@Unique
	LocalPlayer thisPlayer = (LocalPlayer)(Object)this;
	@Inject(method = "<init>", at = @At("TAIL"))
	private void injectOptions(Minecraft minecraft, ClientLevel clientLevel, ClientPacketListener clientPacketListener, StatsCounter statsCounter, ClientRecipeBook clientRecipeBook, boolean bl, boolean bl2, CallbackInfo ci) {
		force100PercentRecharge = CombatifyClient.getInstance().getConfig().misc().force100PercentRecharge();
	}
	@Environment(EnvType.CLIENT)
	@Inject(method = "tick", at = @At("HEAD"))
	public void injectSneakShield(CallbackInfo ci) {
		if (this.hasEnabledShieldOnCrouch()) {
			for (InteractionHand interactionHand : InteractionHand.values()) {
				if ((thisPlayer.isCrouching() && thisPlayer.onGround() || isPassenger()) && !thisPlayer.isUsingItem()) {
					ItemStack itemStack = MethodHandler.getBlockingItem(thisPlayer);

					Item blockingItem = itemStack.getItem();
					boolean bl = Combatify.CONFIG.shieldOnlyWhenCharged() && thisPlayer.getAttackStrengthScale(1.0F) < Combatify.CONFIG.shieldChargePercentage() / 100F && ((ItemExtensions) blockingItem).getBlockingType().requireFullCharge();
					if (!bl && !itemStack.isEmpty() && ((ItemExtensions) blockingItem).getBlockingType().canCrouchBlock() && thisPlayer.getItemInHand(interactionHand) == itemStack) {
						if (!thisPlayer.getCooldowns().isOnCooldown(itemStack.getItem())) {
							((IMinecraft) minecraft).combatify$startUseItem(interactionHand);
							minecraft.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
						}
					}
				} else if ((thisPlayer.isUsingItem() && minecraft.options.keyShift.consumeClick() && !minecraft.options.keyShift.isDown()) && !minecraft.options.keyUse.isDown()) {
					ItemStack itemStack = this.thisPlayer.getItemInHand(interactionHand);
					ItemExtensions item = (ItemExtensions) itemStack.getItem();
					if (!itemStack.isEmpty() && item.getBlockingType().canCrouchBlock() && !item.getBlockingType().isEmpty()) {
						assert minecraft.gameMode != null;
						minecraft.gameMode.releaseUsingItem(thisPlayer);
						startedUsingItem = false;
					}
				}
			}
		}
	}

	@Override
	public void customSwing(InteractionHand interactionHand) {
		swing(interactionHand, false);
		connection.send(new ServerboundSwingPacket(interactionHand));
	}

	@Override
	public boolean isAttackAvailable(float baseTime) {
		if (getAttackStrengthScale(baseTime) < 1.0F && !Combatify.CONFIG.canAttackEarly()) {
			if (force100PercentRecharge.get())
				return false;
			return (getMissedAttackRecovery() && getAttackStrengthStartValue() - (this.attackStrengthTicker - baseTime) > 4.0F);
		}
		return true;
	}

	@ModifyExpressionValue(method = "hasEnoughFoodToStartSprinting", at = @At(value = "CONSTANT", args = "floatValue=6.0F"))
	public float modifyFoodRequirement(float original) {
		return Combatify.CONFIG.oldSprintFoodRequirement() ? -1.0F : original;
	}
    @Redirect(method = "hurtTo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;invulnerableTime:I", opcode = Opcodes.PUTFIELD, ordinal = 0))
    private void syncInvulnerability(LocalPlayer player, int x) {
        player.invulnerableTime = x / 2;
    }

	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V"))
	private void isShieldCrouching(Input instance, boolean b, float v, Operation<Void> original) {
		Item item = MethodHandler.getBlockingItem(thisPlayer).getItem();
		if (thisPlayer.getCooldowns().isOnCooldown(item))
			original.call(instance, b, v);
		else if (((ItemExtensions) item).getBlockingType().canCrouchBlock() && thisPlayer.onGround() && !((ItemExtensions) item).getBlockingType().isEmpty() && !thisPlayer.getCooldowns().isOnCooldown(item)) {
			if (v < 1.0F)
				v = 1.0F;
			original.call(instance, false, v);
		} else
			original.call(instance, b, v);
	}
	@Override
	public float getAttackAnim(float tickDelta) {
		if(((IOptions)Minecraft.getInstance().options).rhythmicAttacks().get()) {
			float var2 = this.attackAnim - this.oAttackAnim;
			if (var2 < 0.0F)
				++var2;

			float var3 = this.oAttackAnim + var2 * tickDelta;
			float charge = Combatify.CONFIG.chargedAttacks() ? 1.95F : 0.9F;
			return var3 > 0.4F && this.getAttackStrengthScale(tickDelta) < charge ? 0.4F + 0.6F * (float)Math.pow((var3 - 0.4F) / 0.6F, 4.0) : var3;
		}
		return super.getAttackAnim(tickDelta);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean hasEnabledShieldOnCrouch() {
		return ((IOptions)minecraft.options).shieldCrouch().get();
	}
}
