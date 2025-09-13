package net.atlas.combatify.mixin.compatibility.cookeymod;

import com.mojang.authlib.GameProfile;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.atlas.combatify.extensions.MiscCategoryExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.rizecookey.cookeymod.CookeyMod;
import org.spongepowered.asm.mixin.Mixin;

@ModSpecific("cookeymod")
@Mixin(LocalPlayer.class)
public abstract class DisableMissedAttackRecoveryMixin extends AbstractClientPlayer implements PlayerExtensions {
	public DisableMissedAttackRecoveryMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Override
	public boolean combatify$isAttackAvailable(float baseTime) {
		if (getAttackStrengthScale(baseTime) < 1.0F && !(Combatify.CONFIG.canAttackEarly() || Combatify.getState().equals(Combatify.CombatifyState.VANILLA))) {
			if (((MiscCategoryExtensions)CookeyMod.getInstance().getConfig().misc()).combatify$force100PercentRecharge().get())
				return false;
			return (combatify$getMissedAttackRecovery() && this.attackStrengthTicker + baseTime > 4.0F);
		}
		return true;
	}
}
