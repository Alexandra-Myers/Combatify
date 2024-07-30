package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import net.atlas.combatify.extensions.IOptions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.InteractionHand;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.atlas.combatify.util.MethodHandler.getBlockingItem;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements PlayerExtensions, LivingEntityExtensions {
	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Shadow
	public abstract void startUsingItem(InteractionHand interactionHand);
	@Unique
	boolean wasShieldBlocking = false;
	@Unique
	InteractionHand shieldBlockingHand = InteractionHand.OFF_HAND;
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
		boolean isBlocking = isBlocking();
		if (isBlocking != wasShieldBlocking) {
			wasShieldBlocking = isBlocking;
			InteractionHand hand = getBlockingItem(thisPlayer).useHand();
			if (isBlocking)
				shieldBlockingHand = hand;
			minecraft.gameRenderer.itemInHandRenderer.itemUsed(hand != null ? hand : shieldBlockingHand);
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

	@ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
	private boolean isShieldCrouching(boolean original) {
		return original || isBlocking();
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
