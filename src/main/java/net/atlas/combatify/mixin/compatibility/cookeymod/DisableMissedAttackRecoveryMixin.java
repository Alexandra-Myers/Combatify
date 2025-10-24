package net.atlas.combatify.mixin.compatibility.cookeymod;

import com.mojang.authlib.GameProfile;
import net.atlas.combatify.extensions.IMiscCategory;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.rizecookey.cookeymod.CookeyMod;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public abstract class DisableMissedAttackRecoveryMixin extends AbstractClientPlayer implements PlayerExtensions {
	public DisableMissedAttackRecoveryMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Override
	public boolean combatify$isAttackAvailable(float baseTime) {
		if (getAttackStrengthScale(baseTime) < 1.0F) {
			if (((IMiscCategory)CookeyMod.getInstance().getConfig().misc()).combatify$getForce100PercentRecharge().get())
				return false;
			return (combatify$getMissedAttackRecovery() && this.attackStrengthTicker + baseTime > 4.0F);
		}
		return true;
	}
}
