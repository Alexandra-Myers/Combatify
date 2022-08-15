package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IMinecraft;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements PlayerExtensions {
	@Unique
	@Final
	@Environment(EnvType.CLIENT)
	public Minecraft minecraft = Minecraft.getInstance();
	LocalPlayer thisPlayer = (LocalPlayer)(Object)this;
	@Environment(EnvType.CLIENT)
	@Inject(method = "tick", at = @At("HEAD"))
	public void injectSneakShield(CallbackInfo ci) {
		if(thisPlayer.isOnGround()) {
			if (this.hasEnabledShieldOnCrouch() && thisPlayer.isCrouching()) {
				for (InteractionHand interactionHand : InteractionHand.values()) {
					ItemStack itemStack = this.thisPlayer.getItemInHand(interactionHand);
					if (!itemStack.isEmpty() && itemStack.getItem() instanceof ShieldItem shieldItem && thisPlayer.isCrouching()) {
						if(!thisPlayer.getCooldowns().isOnCooldown(shieldItem)) {
							((IMinecraft) minecraft).startUseItem(interactionHand);
							if (lowShieldEnabled()) {
								minecraft.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
							}
						}
					}
				}
			} else if ((this.hasEnabledShieldOnCrouch() && thisPlayer.isUsingItem() && minecraft.options.keyShift.consumeClick() && !minecraft.options.keyShift.isDown()) && !minecraft.options.keyUse.isDown()) {
				for (InteractionHand interactionHand : InteractionHand.values()) {
					ItemStack itemStack = this.thisPlayer.getItemInHand(interactionHand);
					if (!itemStack.isEmpty() && itemStack.getItem() instanceof ShieldItem shieldItem) {
						minecraft.gameMode.releaseUsingItem(thisPlayer);
					}
				}
			}
		}
	}

    public LocalPlayerMixin(Minecraft minecraft, ClientLevel clientLevel, ClientPacketListener clientPacketListener, StatsCounter statsCounter, ClientRecipeBook clientRecipeBook, boolean bl, boolean bl2) {
        super(clientLevel, clientPacketListener.getLocalGameProfile(), (ProfilePublicKey)minecraft.getProfileKeyPairManager().profilePublicKey().orElse(null));
    }
    @Redirect(method="hurtTo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;invulnerableTime:I", opcode = Opcodes.PUTFIELD, ordinal=0))
    private void syncInvulnerability(LocalPlayer player, int x) {
        player.invulnerableTime = 5;
    }

	@Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V"))
	private void isShieldCrouching(Input instance, boolean b, float v) {
		for(InteractionHand hand : InteractionHand.values()) {
			if (thisPlayer.isUsingItem() && thisPlayer.getItemInHand(hand).getItem() instanceof ShieldItem) {
				if(v < 1.0F) {
					v = 1.0F;
				}
				instance.tick(false, v);
			} else {
				instance.tick(b, v);
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean hasEnabledShieldOnCrouch() {
		return ((IOptions)minecraft.options).shieldCrouch().get();
	}
	@Environment(EnvType.CLIENT)
	public boolean lowShieldEnabled() {
		return ((IOptions)minecraft.options).lowShield().get();
	}
}
