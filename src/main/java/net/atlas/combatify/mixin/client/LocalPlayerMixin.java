package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.atlas.combatify.util.MethodHandler.getBlockingItem;

@SuppressWarnings("unused")
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
	boolean wasShieldBlocking = false;
	@Unique
	InteractionHand shieldBlockingHand = InteractionHand.OFF_HAND;

	@Shadow
	@Final
	public ClientPacketListener connection;
	@Shadow
	@Final
	protected Minecraft minecraft = Minecraft.getInstance();
	@Unique
	LocalPlayer thisPlayer = (LocalPlayer)(Object)this;
	@OnlyIn(Dist.CLIENT)
	@Inject(method = "tick", at = @At("RETURN"))
	public void injectSneakShield(CallbackInfo ci) {
		if (this.level().hasChunkAt(this.getBlockX(), this.getBlockZ())) {
			boolean isBlocking = isBlocking();
			if (isBlocking != wasShieldBlocking) {
				wasShieldBlocking = isBlocking;
				if (isBlocking) shieldBlockingHand = getBlockingItem(thisPlayer).useHand();
				if (isBlocking && !isUsingItem())
					minecraft.gameRenderer.itemInHandRenderer.itemUsed(shieldBlockingHand);
			}
		}
	}

	@Override
	public void combatify$customSwing(InteractionHand interactionHand) {
		swing(interactionHand, false);
		connection.send(new ServerboundSwingPacket(interactionHand));
	}

	@ModifyExpressionValue(method = "hasEnoughFoodToStartSprinting", at = @At(value = "CONSTANT", args = "floatValue=6.0F"))
	public float modifyFoodRequirement(float original) {
		return Combatify.CONFIG.oldSprintFoodRequirement.get() ? -1.0F : original;
	}
	@WrapOperation(method = "hurtTo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;invulnerableTime:I", opcode = Opcodes.PUTFIELD))
	private void syncInvulnerability(LocalPlayer instance, int value, Operation<Void> original) {
		original.call(instance, value / 2);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean combatify$hasEnabledShieldOnCrouch() {
		return CombatifyClient.shieldCrouch.get();
	}
}
