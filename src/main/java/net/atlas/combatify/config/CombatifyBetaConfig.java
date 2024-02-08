package net.atlas.combatify.config;

import com.google.gson.*;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.BlockingType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.atlas.combatify.Combatify.*;

public class CombatifyBetaConfig extends AtlasConfig {
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
	private IntegerHolder swordProtectionEfficacy;
	private IntegerHolder instantHealthBonus;
	private IntegerHolder shieldChargePercentage;
	private DoubleHolder shieldDisableTime;
	private DoubleHolder cleavingDisableTime;
	private DoubleHolder defenderDisableReduction;
	private DoubleHolder snowballDamage;
	private DoubleHolder eggDamage;
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
		midairKB = createBoolean("midairKB", false);
		fishingHookKB = createBoolean("fishingHookKB", false);
		fistDamage = createBoolean("fistDamage", false);
		swordBlocking = createBoolean("swordBlocking", false);
		shieldOnlyWhenCharged = createBoolean("shieldOnlyWhenCharged", false);
		sprintCritsEnabled = createBoolean("sprintCritsEnabled", true);
		saturationHealing = createBoolean("saturationHealing", false);
		fastHealing = createBoolean("fastHealing", false);
		letVanillaConnect = createBoolean("letVanillaConnect", true);
		oldSprintFoodRequirement = createBoolean("oldSprintFoodRequirement", false);
		projectilesHaveIFrames = createBoolean("projectilesHaveIFrames", false);
		magicHasIFrames = createBoolean("magicHasIFrames", true);
		autoAttackAllowed = createBoolean("autoAttackAllowed", true);
		configOnlyWeapons = createBoolean("configOnlyWeapons", false);
		tieredShields = createBoolean("tieredShields", false);
		piercer = createBoolean("piercer", false);
		defender = createBoolean("defender", false);
		attackReach = createBoolean("attackReach", true);
		attackSpeed = createBoolean("attackSpeed", true);
		instaAttack = createBoolean("instaAttack", false);
		ctsAttackBalancing = createBoolean("ctsAttackBalancing", true);
		eatingInterruption = createBoolean("eatingInterruption", true);
		improvedMiscEntityAttacks = createBoolean("improvedMiscEntityAttacks", false);
		swordProtectionEfficacy = createInRange("swordProtectionEfficacy", 0, -3, 7);
		instantHealthBonus = createInRange("instantHealthBonus", 6, 1, 1000);
		shieldChargePercentage = createInRange("shieldChargePercentage", 195, 1, 200);
		shieldDisableTime = createInRange("shieldDisableTime", 1.6, 0, 10);
		cleavingDisableTime = createInRange("cleavingDisableTime", 0.5, 0, 10);
		defenderDisableReduction = createInRange("defenderDisableReduction", 0.5, 0, 10);
		snowballDamage = createInRange("snowballDamage", 0, 0, 40D);
		eggDamage = createInRange("eggDamage", 0, 0, 40D);
		bowUncertainty = createInRange("bowUncertainty", 0.25, 0, 4);
		baseHandAttackSpeed = createInRange("baseHandAttackSpeed", 2.5, 2.5, 20);
		minHitboxSize = createInRange("minHitboxSize", 0.9, 0, 5);
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
	public Integer swordProtectionEfficacy() {
		return swordProtectionEfficacy.get();
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
