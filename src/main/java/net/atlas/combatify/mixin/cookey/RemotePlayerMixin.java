package net.atlas.combatify.mixin.cookey;

import com.mojang.authlib.GameProfile;
import net.atlas.combatify.CombatifyClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RemotePlayer.class)
public abstract class RemotePlayerMixin extends AbstractClientPlayer {

    BooleanOption fixLocalPlayerHandling = CombatifyClient.getInstance().getConfig().misc().fixLocalPlayerHandling();

    private RemotePlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void fixLocalPlayerHandling(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        if (fixLocalPlayerHandling.get()) cir.setReturnValue(false);
    }
}
