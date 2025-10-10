package net.atlas.combatify.mixin.compatibility.cookeymod;

import com.mojang.authlib.GameProfile;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.atlas.combatify.extensions.MiscCategoryExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.rizecookey.cookeymod.CookeyMod;
import org.spongepowered.asm.mixin.Mixin;

@ModSpecific("cookeymod")
@Mixin(LocalPlayer.class)
public abstract class DisableMissedAttackRecoveryMixin extends AbstractClientPlayer implements PlayerExtensions {
	public DisableMissedAttackRecoveryMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Override
	public boolean combatify$isAttackAvailable(float baseTime, ItemStack weapon) {
		float minAttackCharge = weapon.getOrDefault(DataComponents.MINIMUM_ATTACK_CHARGE, Combatify.CONFIG.canAttackEarly() || Combatify.getState().equals(Combatify.CombatifyState.VANILLA) ? 0.0F : Combatify.CONFIG.chargedAttacks() ? 0.5F : 1.0F);
		if (!Combatify.getState().equals(Combatify.CombatifyState.VANILLA) && Combatify.CONFIG.chargedAttacks()) minAttackCharge *= 2;
		float strengthScale = getAttackStrengthScale(baseTime);
		if (minAttackCharge > 0.0F && strengthScale < minAttackCharge) {
			if (weapon.has(DataComponents.MINIMUM_ATTACK_CHARGE) || Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return false; // Don't allow weapons with custom minimum attack charge to use missed attack recovery
			if (((MiscCategoryExtensions)CookeyMod.getInstance().getConfig().misc()).combatify$force100PercentRecharge().get()) return false;
			return (combatify$getMissedAttackRecovery() && this.attackStrengthTicker + baseTime > 4.0F);
		}
		return true;
	}
}
