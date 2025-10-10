package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.authlib.GameProfile;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.wrapper.PlayerWrapper;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
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
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements PlayerExtensions {
	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Shadow
	public abstract void startUsingItem(@NotNull InteractionHand interactionHand);
	@Unique
	boolean wasShieldBlocking = false;
	@Unique
	InteractionHand shieldBlockingHand = InteractionHand.OFF_HAND;
	@Shadow
	@Final
	public ClientPacketListener connection;

	@Shadow
	public abstract boolean isUsingItem();

	@Shadow
	@Final
	protected Minecraft minecraft;
	@Unique
	LocalPlayer thisPlayer = (LocalPlayer)(Object)this;
	@Environment(EnvType.CLIENT)
	@Inject(method = "tick", at = @At("RETURN"))
	public void injectSneakShield(CallbackInfo ci) {
		if (this.hasClientLoaded()) {
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

	@ModifyExpressionValue(method = "hasEnoughFoodToSprint", at = @At(value = "CONSTANT", args = "floatValue=6.0F"))
	public float modifyFoodRequirement(float original) {
		return Combatify.getState().equals(Combatify.CombatifyState.VANILLA) ? original : (float) Combatify.CONFIG.getFoodImpl().execGetterFunc(original, "getMinimumSprintLevel(player)", new PlayerWrapper<>(thisPlayer));
	}
    @Redirect(method = "hurtTo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;invulnerableTime:I", opcode = Opcodes.PUTFIELD, ordinal = 0))
    private void syncInvulnerability(LocalPlayer player, int x) {
        player.invulnerableTime = x / 2;
    }

	@ModifyExpressionValue(method = "isSlowDueToUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
	private boolean isShieldCrouching(boolean original) {
		return original || isBlocking();
	}
	@ModifyReceiver(method = "isSlowDueToUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	private ItemStack isShieldCrouching(ItemStack instance, DataComponentType<?> dataComponentType, Object o) {
		ItemStack blockingItem = MethodHandler.getBlockingItem(thisPlayer).stack();
		return blockingItem.isEmpty() ? instance : blockingItem;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean combatify$hasEnabledShieldOnCrouch() {
		return CombatifyClient.shieldCrouch.get();
	}
}
