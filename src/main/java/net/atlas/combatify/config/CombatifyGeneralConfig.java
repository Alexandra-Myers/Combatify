package net.atlas.combatify.config;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import net.atlas.atlaslib.AtlasLib;
import net.atlas.atlaslib.config.AtlasConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.atlas.combatify.Combatify.*;

public class CombatifyGeneralConfig extends AtlasConfig {
	private BooleanHolder weaponTypesEnabled;
	private BooleanHolder iFramesBasedOnWeapon;
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
	private BooleanHolder dispensableTridents;
	private BooleanHolder snowballKB;
	private BooleanHolder resetOnItemChange;
	private BooleanHolder sweepWithSweeping;
	private BooleanHolder sweepingNegatedForTamed;
	private BooleanHolder ctsMomentumPassedToProjectiles;
	private BooleanHolder swingThroughGrass;
	private BooleanHolder strengthAppliesToEnchants;
	private BooleanHolder percentageDamageEffects;
	private BooleanHolder ctsKB;
	private BooleanHolder tridentVoidReturn;
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
	private BooleanHolder attackReach;
	private BooleanHolder armorPiercingDisablesShields;
	private BooleanHolder attackSpeed;
	private BooleanHolder instaAttack;
	private BooleanHolder ctsAttackBalancing;
	private BooleanHolder eatingInterruption;
	private BooleanHolder improvedMiscEntityAttacks;
	private IntegerHolder shieldDelay;
	private IntegerHolder instantHealthBonus;
	private IntegerHolder attackDecayMinCharge;
	private IntegerHolder attackDecayMaxCharge;
	private IntegerHolder attackDecayMinPercentage;
	private IntegerHolder attackDecayMaxPercentage;
	private IntegerHolder shieldChargePercentage;
	private DoubleHolder healingTime;
	private DoubleHolder instantTippedArrowEffectMultiplier;
	private DoubleHolder shieldDisableTime;
	private DoubleHolder breachArmorPiercing;
	private DoubleHolder snowballDamage;
	private DoubleHolder eggDamage;
	private DoubleHolder windChargeDamage;
	private DoubleHolder bowUncertainty;
	private DoubleHolder crossbowUncertainty;
	private DoubleHolder baseHandAttackSpeed;
	private DoubleHolder minHitboxSize;
	private DoubleHolder thrownTridentDamage;
	private EnumHolder<ArrowDisableMode> arrowDisableMode;
	private EnumHolder<ArmourPiercingMode> armourPiercingMode;
	private Category ctsB;
	private Category ctsI;
	private Category ctsD;
	private Category extraB;
	private Category extraI;
	private Category extraD;
	private Category extraE;

	public CombatifyGeneralConfig() {
		super(id("combatify-general"));
		declareDefaultForMod("combatify");
	}

	@Override
	protected InputStream getDefaultedConfig() {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("combatify-general.json");
	}

	@Override
	public void saveExtra(JsonWriter jsonWriter, PrintWriter printWriter) {

	}

	@Override
	public void defineConfigHolders() {
		attackDecay = createBoolean("attackDecay", false);
		attackDecay.tieToCategory(ctsB);
		attackDecay.setupTooltip(1);
		attackReach = createBoolean("attackReach", true);
		attackReach.tieToCategory(ctsB);
		attackReach.setupTooltip(1);
		autoAttackAllowed = createBoolean("autoAttackAllowed", true);
		autoAttackAllowed.tieToCategory(ctsB);
		autoAttackAllowed.setupTooltip(1);
		bedrockImpaling = createBoolean("bedrockImpaling", true);
		bedrockImpaling.tieToCategory(ctsB);
		bedrockImpaling.setupTooltip(1);
		bowFatigue = createBoolean("bowFatigue", true);
		bowFatigue.tieToCategory(ctsB);
		bowFatigue.setupTooltip(4);
		canAttackEarly = createBoolean("canAttackEarly", false);
		canAttackEarly.tieToCategory(ctsB);
		canAttackEarly.setupTooltip(1);
		canSweepOnMiss = createBoolean("canSweepOnMiss", true);
		canSweepOnMiss.tieToCategory(ctsB);
		canSweepOnMiss.setupTooltip(1);
		chargedAttacks = createBoolean("chargedAttacks", true);
		chargedAttacks.tieToCategory(ctsB);
		chargedAttacks.setupTooltip(1);
		chargedReach = createBoolean("chargedReach", true);
		chargedReach.tieToCategory(ctsB);
		chargedReach.setupTooltip(1);
		creativeReach = createBoolean("creativeReach", false);
		creativeReach.tieToCategory(ctsB);
		creativeReach.setupTooltip(1);
		ctsAttackBalancing = createBoolean("ctsAttackBalancing", true);
		ctsAttackBalancing.tieToCategory(ctsB);
		ctsAttackBalancing.setupTooltip(1);
		ctsKB = createBoolean("ctsKB", true);
		ctsKB.tieToCategory(ctsB);
		ctsKB.setupTooltip(1);
		ctsMomentumPassedToProjectiles = createBoolean("ctsMomentumPassedToProjectiles", true);
		ctsMomentumPassedToProjectiles.tieToCategory(ctsB);
		ctsMomentumPassedToProjectiles.setupTooltip(1);
		dispensableTridents = createBoolean("dispensableTridents", true);
		dispensableTridents.tieToCategory(ctsB);
		dispensableTridents.setupTooltip(1);
		eatingInterruption = createBoolean("eatingInterruption", true);
		eatingInterruption.tieToCategory(ctsB);
		eatingInterruption.setupTooltip(1);
		fistDamage = createBoolean("fistDamage", false);
		fistDamage.tieToCategory(ctsB);
		fistDamage.setupTooltip(1);
		hasMissTime = createBoolean("hasMissTime", false);
		hasMissTime.tieToCategory(ctsB);
		hasMissTime.setupTooltip(1);
		iFramesBasedOnWeapon = createBoolean("iFramesBasedOnWeapon", true);
		iFramesBasedOnWeapon.tieToCategory(ctsB);
		iFramesBasedOnWeapon.setupTooltip(1);
		missedAttackRecovery = createBoolean("missedAttackRecovery", true);
		missedAttackRecovery.tieToCategory(ctsB);
		missedAttackRecovery.setupTooltip(1);
		percentageDamageEffects = createBoolean("percentageDamageEffects", true);
		percentageDamageEffects.tieToCategory(ctsB);
		percentageDamageEffects.setupTooltip(1);
		projectilesHaveIFrames = createBoolean("projectilesHaveIFrames", false);
		projectilesHaveIFrames.tieToCategory(ctsB);
		projectilesHaveIFrames.setupTooltip(1);
		resetOnItemChange = createBoolean("resetOnItemChange", false);
		resetOnItemChange.tieToCategory(ctsB);
		resetOnItemChange.setupTooltip(1);
		fastHealing = createBoolean("fastHealing", false);
		fastHealing.tieToCategory(ctsB);
		fastHealing.setupTooltip(1);
		saturationHealing = createBoolean("saturationHealing", false);
		saturationHealing.tieToCategory(ctsB);
		saturationHealing.setupTooltip(1);
		snowballKB = createBoolean("snowballKB", true);
		snowballKB.tieToCategory(ctsB);
		snowballKB.setupTooltip(1);
		sprintCritsEnabled = createBoolean("sprintCritsEnabled", true);
		sprintCritsEnabled.tieToCategory(ctsB);
		sprintCritsEnabled.setupTooltip(1);
		strengthAppliesToEnchants = createBoolean("strengthAppliesToEnchants", true);
		strengthAppliesToEnchants.tieToCategory(ctsB);
		strengthAppliesToEnchants.setupTooltip(1);
		sweepWithSweeping = createBoolean("sweepWithSweeping", true);
		sweepWithSweeping.tieToCategory(ctsB);
		sweepWithSweeping.setupTooltip(1);
		swingThroughGrass = createBoolean("swingThroughGrass", true);
		swingThroughGrass.tieToCategory(ctsB);
		swingThroughGrass.setupTooltip(1);
		tridentVoidReturn = createBoolean("tridentVoidReturn", true);
		tridentVoidReturn.tieToCategory(ctsB);
		tridentVoidReturn.setupTooltip(1);
		weaponTypesEnabled = createBoolean("weaponTypesEnabled", true);
		weaponTypesEnabled.tieToCategory(ctsB);
		weaponTypesEnabled.setupTooltip(3);

		shieldDelay = createInRange("shieldDelay", 0, 0, 2000, false);
		shieldDelay.tieToCategory(ctsI);
		shieldDelay.setupTooltip(1);
		instantHealthBonus = createInRange("instantHealthBonus", 6, 1, 1000, false);
		instantHealthBonus.tieToCategory(ctsI);

		baseHandAttackSpeed = createInRange("baseHandAttackSpeed", 2.5, 2.5, 20);
		baseHandAttackSpeed.tieToCategory(ctsD);
		bowUncertainty = createInRange("bowUncertainty", 0.25, 0, 4);
		bowUncertainty.tieToCategory(ctsD);
		crossbowUncertainty = createInRange("crossbowUncertainty", 0.25, 0, 4);
		crossbowUncertainty.tieToCategory(ctsD);
		healingTime = createInRange("healingTime", 2, 0, 100D);
		healingTime.tieToCategory(ctsD);
		healingTime.setupTooltip(1);
		instantTippedArrowEffectMultiplier = createInRange("instantTippedArrowEffectMultiplier", 0.125, 0, 4);
		instantTippedArrowEffectMultiplier.tieToCategory(ctsD);
		instantTippedArrowEffectMultiplier.setupTooltip(1);
		shieldDisableTime = createInRange("shieldDisableTime", 1.6, 0, 10);
		shieldDisableTime.tieToCategory(ctsD);
		shieldDisableTime.setupTooltip(1);
		minHitboxSize = createInRange("minHitboxSize", 0.9, 0, 5);
		minHitboxSize.tieToCategory(ctsD);
		minHitboxSize.setupTooltip(1);

		armorPiercingDisablesShields = createBoolean("armorPiercingDisablesShields", false);
		armorPiercingDisablesShields.tieToCategory(extraB);
		armorPiercingDisablesShields.setupTooltip(1);
		attackSpeed = createBoolean("attackSpeed", true);
		attackSpeed.tieToCategory(extraB);
		attackSpeed.setupTooltip(1);
		canInteractWhenCrouchShield = createBoolean("canInteractWhenCrouchShield", true);
		canInteractWhenCrouchShield.tieToCategory(extraB);
		configOnlyWeapons = createBoolean("configOnlyWeapons", false);
		configOnlyWeapons.tieToCategory(extraB);
		configOnlyWeapons.setRestartRequired(true);
		configOnlyWeapons.setupTooltip(1);
		tieredShields = createBoolean("tieredShields", false);
		tieredShields.tieToCategory(extraB);
		tieredShields.setRestartRequired(true);
		tieredShields.setupTooltip(1);
		disableDuringShieldDelay = createBoolean("disableDuringShieldDelay", false);
		disableDuringShieldDelay.tieToCategory(extraB);
		disableDuringShieldDelay.setupTooltip(1);
		fishingHookKB = createBoolean("fishingHookKB", false);
		fishingHookKB.tieToCategory(extraB);
		fishingHookKB.setupTooltip(1);
		improvedMiscEntityAttacks = createBoolean("improvedMiscEntityAttacks", false);
		improvedMiscEntityAttacks.tieToCategory(extraB);
		improvedMiscEntityAttacks.setupTooltip(1);
		instaAttack = createBoolean("instaAttack", false);
		instaAttack.tieToCategory(extraB);
		instaAttack.setupTooltip(1);
		letVanillaConnect = createBoolean("letVanillaConnect", true);
		letVanillaConnect.tieToCategory(extraB);
		letVanillaConnect.setupTooltip(1);
		magicHasIFrames = createBoolean("magicHasIFrames", true);
		magicHasIFrames.tieToCategory(extraB);
		magicHasIFrames.setupTooltip(1);
		midairKB = createBoolean("midairKB", false);
		midairKB.tieToCategory(extraB);
		midairKB.setupTooltip(2);
		oldSprintFoodRequirement = createBoolean("oldSprintFoodRequirement", false);
		oldSprintFoodRequirement.tieToCategory(extraB);
		oldSprintFoodRequirement.setupTooltip(1);
		shieldOnlyWhenCharged = createBoolean("shieldOnlyWhenCharged", false);
		shieldOnlyWhenCharged.tieToCategory(extraB);
		shieldOnlyWhenCharged.setupTooltip(2);
		sweepingNegatedForTamed = createBoolean("sweepingNegatedForTamed", false);
		sweepingNegatedForTamed.tieToCategory(extraB);
		sweepingNegatedForTamed.setupTooltip(1);
		swordBlocking = createBoolean("swordBlocking", false);
		swordBlocking.tieToCategory(extraB);
		swordBlocking.setupTooltip(1);

		attackDecayMinCharge = createInRange("attackDecayMinCharge", 0, 0, 200, false);
		attackDecayMinCharge.tieToCategory(extraI);
		attackDecayMinCharge.setupTooltip(1);
		attackDecayMaxCharge = createInRange("attackDecayMaxCharge", 100, 0, 200, false);
		attackDecayMaxCharge.tieToCategory(extraI);
		attackDecayMaxCharge.setupTooltip(1);
		attackDecayMinPercentage = createInRange("attackDecayMinPercentage", 0, 0, 200, false);
		attackDecayMinPercentage.tieToCategory(extraI);
		attackDecayMinPercentage.setupTooltip(2);
		attackDecayMaxPercentage = createInRange("attackDecayMaxPercentage", 100, 0, 200, false);
		attackDecayMaxPercentage.tieToCategory(extraI);
		attackDecayMaxPercentage.setupTooltip(2);
		shieldChargePercentage = createInRange("shieldChargePercentage", 195, 1, 200, true);
		shieldChargePercentage.tieToCategory(extraI);
		shieldChargePercentage.setupTooltip(1);

		breachArmorPiercing = createInRange("breachArmorPiercing", 0.15, 0, 1);
		breachArmorPiercing.tieToCategory(extraD);
		breachArmorPiercing.setupTooltip(2);
		eggDamage = createInRange("eggDamage", 0, 0, 40D);
		eggDamage.tieToCategory(extraD);
		snowballDamage = createInRange("snowballDamage", 0, 0, 40D);
		snowballDamage.tieToCategory(extraD);
		windChargeDamage = createInRange("windChargeDamage", 1, 0, 40D);
		windChargeDamage.tieToCategory(extraD);
		thrownTridentDamage = createInRange("thrownTridentDamage", 8, 0, 40D);
		thrownTridentDamage.tieToCategory(extraD);

		arrowDisableMode = createEnum("arrowDisableMode", ArrowDisableMode.NONE, ArrowDisableMode.class, ArrowDisableMode.values(), e -> Component.translatable("text.config.combatify-general.option.arrowDisableMode." + e.name().toLowerCase(Locale.ROOT)));
		arrowDisableMode.tieToCategory(extraE);
		arrowDisableMode.setupTooltip(7);
		armourPiercingMode = createEnum("armourPiercingMode", ArmourPiercingMode.VANILLA, ArmourPiercingMode.class, ArmourPiercingMode.values(), e -> Component.translatable("text.config.combatify-general.option.armourPiercingMode." + e.name().toLowerCase(Locale.ROOT)));
		armourPiercingMode.tieToCategory(extraE);
		armourPiercingMode.setupTooltip(4);
	}

	@Override
	public @NotNull List<Category> createCategories() {
		List<Category> categoryList = super.createCategories();
		ctsB = new Category(this, "cts_booleans", new ArrayList<>());
		ctsI = new Category(this, "cts_integers", new ArrayList<>());
		ctsD = new Category(this, "cts_doubles", new ArrayList<>());
		extraB = new Category(this, "extra_booleans", new ArrayList<>());
		extraI = new Category(this, "extra_integers", new ArrayList<>());
		extraD = new Category(this, "extra_doubles", new ArrayList<>());
		extraE = new Category(this, "extra_enums", new ArrayList<>());
		categoryList.add(ctsB);
		categoryList.add(ctsI);
		categoryList.add(ctsD);
		categoryList.add(extraB);
		categoryList.add(extraI);
		categoryList.add(extraD);
		categoryList.add(extraE);
		return categoryList;
	}

	@Override
	public void resetExtraHolders() {

	}

	@Override
	public <T> void alertChange(ConfigValue<T> tConfigValue, T newValue) {

	}

	@Override
	protected void loadExtra(JsonObject jsonObject) {

	}

	@Override
	public void handleExtraSync(AtlasLib.AtlasConfigPacket packet, LocalPlayer player, PacketSender sender) {

	}

	@Override
	@Environment(EnvType.CLIENT)
	public Screen createScreen(Screen prevScreen) {
		return null;
	}

	public Boolean weaponTypesEnabled() {
		return weaponTypesEnabled.get();
	}
	public Boolean iFramesBasedOnWeapon() {
		return iFramesBasedOnWeapon.get();
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
	public Boolean resetOnItemChange() {
		return resetOnItemChange.get();
	}
	public Boolean sweepWithSweeping() {
		return sweepWithSweeping.get();
	}
	public Boolean sweepingNegatedForTamed() {
		return sweepingNegatedForTamed.get();
	}
	public Boolean ctsMomentumPassedToProjectiles() {
		return ctsMomentumPassedToProjectiles.get();
	}
	public Boolean swingThroughGrass() {
		return swingThroughGrass.get();
	}
	public Boolean strengthAppliesToEnchants() {
		return strengthAppliesToEnchants.get();
	}
	public Boolean percentageDamageEffects() {
		return percentageDamageEffects.get();
	}
	public Boolean ctsKB() {
		return ctsKB.get();
	}
	public Boolean tridentVoidReturn() {
		return tridentVoidReturn.get();
	}
	public Boolean dispensableTridents() {
		return dispensableTridents.get();
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
	public Boolean attackReach() {
		return attackReach.get();
	}
	public Boolean armorPiercingDisablesShields() {
		return armorPiercingDisablesShields.get();
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
	public double attackDecayMinCharge() {
		return attackDecayMinCharge.get().doubleValue() / 100;
	}
	public double attackDecayMaxCharge() {
		return attackDecayMaxCharge.get().doubleValue() / 100;
	}
	public double attackDecayMaxChargeDiff() {
		double ret = attackDecayMaxCharge() - attackDecayMinCharge();
		if (ret <= 0)
			ret = 1;
		return ret;
	}
	public double attackDecayMinPercentage() {
		return attackDecayMinPercentage.get().doubleValue() / 100;
	}
	public double attackDecayMaxPercentageDiff() {
		return (attackDecayMaxPercentage.get().doubleValue() / 100) - attackDecayMinPercentage();
	}
	public Integer shieldChargePercentage() {
		return shieldChargePercentage.get();
	}
	public Double healingTime() {
		return healingTime.get();
	}
	public Double instantTippedArrowEffectMultiplier() {
		return instantTippedArrowEffectMultiplier.get();
	}
	public Double shieldDisableTime() {
		return shieldDisableTime.get();
	}
	public Double breachArmorPiercing() {
		return breachArmorPiercing.get();
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
	public Double thrownTridentDamage() {
		return thrownTridentDamage.get();
	}
	public Double bowUncertainty() {
		return bowUncertainty.get();
	}
	public Double crossbowUncertainty() {
		return crossbowUncertainty.get();
	}
	public Double baseHandAttackSpeed() {
		return baseHandAttackSpeed.get();
	}
	public Double minHitboxSize() {
		return minHitboxSize.get();
	}
	public ArrowDisableMode arrowDisableMode() {
		return arrowDisableMode.get();
	}
	public ArmourPiercingMode armourPiercingMode() {
		return armourPiercingMode.get();
	}
}
