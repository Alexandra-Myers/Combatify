package net.atlas.combatify.config;

import com.google.gson.*;
import net.atlas.combatify.networking.NetworkingHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.player.LocalPlayer;

import java.io.InputStream;

import static net.atlas.combatify.Combatify.*;

public class CombatifyBetaConfig extends AtlasConfig {
	private BooleanHolder weaponTypesEnabled;
	private BooleanHolder bowFatigue;
	private BooleanHolder chargedAttacks;
	private BooleanHolder canAttackEarly;
	private BooleanHolder canSweepOnMiss;
	private BooleanHolder chargedReach;
	private BooleanHolder creativeReach;
	private BooleanHolder attackDecay;
	private BooleanHolder missedAttackRecovery;
	private BooleanHolder disableDuringShieldDelay;
	private BooleanHolder hasMissTime;
	private BooleanHolder canInteractWhenCrouchShield;
	private BooleanHolder bedrockImpaling;
	private BooleanHolder snowballKB;
	private BooleanHolder midairKB;
	private BooleanHolder fishingHookKB;
	private BooleanHolder fistDamage;
	private BooleanHolder swordBlocking;
	private BooleanHolder shieldOnlyWhenCharged;
	private BooleanHolder sprintCritsEnabled;
	private BooleanHolder saturationHealing;
	private BooleanHolder fastHealing;
	private BooleanHolder letVanillaConnect;
	private BooleanHolder oldSprintFoodRequirement;
	private BooleanHolder projectilesHaveIFrames;
	private BooleanHolder magicHasIFrames;
	private BooleanHolder autoAttackAllowed;
	private BooleanHolder configOnlyWeapons;
	private BooleanHolder tieredShields;
	private BooleanHolder piercer;
	private BooleanHolder defender;
	private BooleanHolder attackReach;
	private BooleanHolder attackSpeed;
	private BooleanHolder instaAttack;
	private BooleanHolder ctsAttackBalancing;
	private BooleanHolder eatingInterruption;
	private BooleanHolder improvedMiscEntityAttacks;
	private IntegerHolder shieldDelay;
	private IntegerHolder instantHealthBonus;
	private IntegerHolder shieldChargePercentage;
	private DoubleHolder shieldDisableTime;
	private DoubleHolder cleavingDisableTime;
	private DoubleHolder defenderDisableReduction;
	private DoubleHolder snowballDamage;
	private DoubleHolder eggDamage;
	private DoubleHolder windChargeDamage;
	private DoubleHolder bowUncertainty;
	private DoubleHolder baseHandAttackSpeed;
	private DoubleHolder minHitboxSize;

	public CombatifyBetaConfig() {
		super(id("combatify-beta"));
	}

	@Override
	protected InputStream getDefaultedConfig() {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("combatify-beta.json");
	}

	@Override
	public void defineConfigHolders() {
		attackDecay = createBoolean("attackDecay", false);
		attackReach = createBoolean("attackReach", true);
		autoAttackAllowed = createBoolean("autoAttackAllowed", true);
		bedrockImpaling = createBoolean("bedrockImpaling", true);
		bowFatigue = createBoolean("bowFatigue", true);
		canAttackEarly = createBoolean("canAttackEarly", false);
		canSweepOnMiss = createBoolean("canSweepOnMiss", true);
		chargedAttacks = createBoolean("chargedAttacks", true);
		chargedReach = createBoolean("chargedReach", true);
		creativeReach = createBoolean("creativeReach", false);
		ctsAttackBalancing = createBoolean("ctsAttackBalancing", true);
		eatingInterruption = createBoolean("eatingInterruption", true);
		fistDamage = createBoolean("fistDamage", false);
		hasMissTime = createBoolean("hasMissTime", false);
		missedAttackRecovery = createBoolean("missedAttackRecovery", true);
		projectilesHaveIFrames = createBoolean("projectilesHaveIFrames", false);
		fastHealing = createBoolean("fastHealing", false);
		saturationHealing = createBoolean("saturationHealing", false);
		snowballKB = createBoolean("snowballKB", true);
		sprintCritsEnabled = createBoolean("sprintCritsEnabled", true);
		weaponTypesEnabled = createBoolean("weaponTypesEnabled", true);

		shieldDelay = createInRange("shieldDelay", 0, 0, 100);
		instantHealthBonus = createInRange("instantHealthBonus", 6, 1, 1000);

		baseHandAttackSpeed = createInRange("baseHandAttackSpeed", 2.5, 2.5, 20);
		bowUncertainty = createInRange("bowUncertainty", 0.25, 0, 4);
		cleavingDisableTime = createInRange("cleavingDisableTime", 0.5, 0, 10);
		shieldDisableTime = createInRange("shieldDisableTime", 1.6, 0, 10);
		minHitboxSize = createInRange("minHitboxSize", 0.9, 0, 5);

		attackSpeed = createBoolean("attackSpeed", true);
		canInteractWhenCrouchShield = createBoolean("canInteractWhenCrouchShield", true);
		configOnlyWeapons = createBoolean("configOnlyWeapons", false);
		defender = createBoolean("defender", false);
		piercer = createBoolean("piercer", false);
		tieredShields = createBoolean("tieredShields", false);
		disableDuringShieldDelay = createBoolean("disableDuringShieldDelay", false);
		fishingHookKB = createBoolean("fishingHookKB", false);
		improvedMiscEntityAttacks = createBoolean("improvedMiscEntityAttacks", false);
		instaAttack = createBoolean("instaAttack", false);
		letVanillaConnect = createBoolean("letVanillaConnect", true);
		magicHasIFrames = createBoolean("magicHasIFrames", true);
		midairKB = createBoolean("midairKB", false);
		oldSprintFoodRequirement = createBoolean("oldSprintFoodRequirement", false);
		shieldOnlyWhenCharged = createBoolean("shieldOnlyWhenCharged", false);
		swordBlocking = createBoolean("swordBlocking", false);

		shieldChargePercentage = createInRange("shieldChargePercentage", 195, 1, 200);

		defenderDisableReduction = createInRange("defenderDisableReduction", 0.5, 0, 10);
		eggDamage = createInRange("eggDamage", 0, 0, 40D);
		snowballDamage = createInRange("snowballDamage", 0, 0, 40D);
		windChargeDamage = createInRange("windChargeDamage", 1, 0, 40D);
	}

	@Override
	public <T> void alertChange(AtlasConfig.ConfigValue<T> tConfigValue, T newValue) {

	}

	@Override
	protected void loadExtra(JsonObject jsonObject) {

	}

	@Override
	public void handleExtraSync(NetworkingHandler.AtlasConfigPacket packet, LocalPlayer player, PacketSender sender) {

	}
	public Boolean weaponTypesEnabled() {
		return weaponTypesEnabled.get();
	}
	public Boolean bowFatigue() {
		return bowFatigue.get();
	}
	public Boolean canAttackEarly() {
		return canAttackEarly.get();
	}
	public Boolean chargedAttacks() {
		return chargedAttacks.get();
	}
	public Boolean chargedReach() {
		return chargedReach.get();
	}
	public Boolean creativeReach() {
		return creativeReach.get();
	}
	public Boolean attackDecay() {
		return attackDecay.get();
	}
	public Boolean missedAttackRecovery() {
		return missedAttackRecovery.get();
	}
	public Boolean canSweepOnMiss() {
		return canSweepOnMiss.get();
	}
	public Boolean canInteractWhenCrouchShield() {
		return canInteractWhenCrouchShield.get();
	}
	public Boolean hasMissTime() {
		return hasMissTime.get();
	}
	public Boolean disableDuringShieldDelay() {
		return disableDuringShieldDelay.get();
	}
	public Boolean bedrockImpaling() {
		return bedrockImpaling.get();
	}
	public Boolean snowballKB() {
		return snowballKB.get();
	}
	public Boolean midairKB() {
		return midairKB.get();
	}
	public Boolean fishingHookKB() {
		return fishingHookKB.get();
	}
	public Boolean fistDamage() {
		return fistDamage.get();
	}
	public Boolean swordBlocking() {
		return swordBlocking.get();
	}
	public Boolean shieldOnlyWhenCharged() {
		return shieldOnlyWhenCharged.get();
	}
	public Boolean sprintCritsEnabled() {
		return sprintCritsEnabled.get();
	}
	public Boolean saturationHealing() {
		return saturationHealing.get();
	}
	public Boolean fastHealing() {
		return fastHealing.get();
	}
	public Boolean letVanillaConnect() {
		return letVanillaConnect.get();
	}
	public Boolean oldSprintFoodRequirement() {
		return oldSprintFoodRequirement.get();
	}
	public Boolean projectilesHaveIFrames() {
		return projectilesHaveIFrames.get();
	}
	public Boolean magicHasIFrames() {
		return magicHasIFrames.get();
	}
	public Boolean autoAttackAllowed() {
		return autoAttackAllowed.get();
	}
	public Boolean configOnlyWeapons() {
		return configOnlyWeapons.get();
	}
	public Boolean tieredShields() {
		return tieredShields.get();
	}
	public Boolean piercer() {
		return piercer.get();
	}
	public Boolean defender() {
		return defender.get();
	}
	public Boolean attackReach() {
		return attackReach.get();
	}
	public Boolean attackSpeed() {
		return attackSpeed.get();
	}
	public Boolean instaAttack() {
		return instaAttack.get();
	}
	public Boolean ctsAttackBalancing() {
		return ctsAttackBalancing.get();
	}
	public Boolean eatingInterruption() {
		return eatingInterruption.get();
	}
	public Boolean improvedMiscEntityAttacks() {
		return improvedMiscEntityAttacks.get();
	}
	public Integer shieldDelay() {
		return shieldDelay.get();
	}
	public Integer instantHealthBonus() {
		return instantHealthBonus.get();
	}
	public Integer shieldChargePercentage() {
		return shieldChargePercentage.get();
	}
	public Double shieldDisableTime() {
		return shieldDisableTime.get();
	}
	public Double cleavingDisableTime() {
		return cleavingDisableTime.get();
	}
	public Double defenderDisableReduction() {
		return defenderDisableReduction.get();
	}
	public Double snowballDamage() {
		return snowballDamage.get();
	}
	public Double eggDamage() {
		return eggDamage.get();
	}
	public Double windChargeDamage() {
		return windChargeDamage.get();
	}
	public Double bowUncertainty() {
		return bowUncertainty.get();
	}
	public Double baseHandAttackSpeed() {
		return baseHandAttackSpeed.get();
	}
	public Double minHitboxSize() {
		return minHitboxSize.get();
	}
}
