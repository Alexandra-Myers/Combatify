package net.atlas.combatify.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeConfig {

	public BooleanOption toolsAreWeapons;
	public BooleanOption midairKB;
	public BooleanOption fishingHookKB;
	public BooleanOption fistDamage;
	public BooleanOption swordBlocking;
	public BooleanOption shieldOnlyWhenCharged;
	public BooleanOption sprintCritsEnabled;
	public BooleanOption saturationHealing;
	public BooleanOption fastHealing;
	public BooleanOption letVanillaConnect;
	public BooleanOption oldSprintFoodRequirement;
	public BooleanOption projectilesHaveIFrames;
	public BooleanOption magicHasIFrames;
	public BooleanOption autoAttackAllowed;
	public BooleanOption configOnlyWeapons;
	public BooleanOption tieredShields;
	public BooleanOption piercer;
	public BooleanOption defender;
	public BooleanOption attackReach;
	public BooleanOption attackSpeed;
	public BooleanOption instaAttack;
	public BooleanOption ctsAttackBalancing;
	public BooleanOption eatingInterruption;
	public BooleanOption improvedMiscEntityAttacks;
	public IntOption swordProtectionEfficacy;
	public IntOption potionUseDuration;
	public IntOption honeyBottleUseDuration;
	public IntOption milkBucketUseDuration;
	public IntOption stewUseDuration;
	public IntOption instantHealthBonus;
	public IntOption shieldChargePercentage;
	public DoubleOption shieldDisableTime;
	public DoubleOption cleavingDisableTime;
	public DoubleOption defenderDisableReduction;
	public DoubleOption snowballDamage;
	public DoubleOption eggDamage;
	public DoubleOption bowUncertainty;
	public DoubleOption baseHandAttackSpeed;
	public DoubleOption slowestToolAttackSpeed;
	public DoubleOption slowToolAttackSpeed;
	public DoubleOption fastToolAttackSpeed;
	public DoubleOption fastestToolAttackSpeed;
	public BiMap<String, SynchableOption<?>> options = HashBiMap.create();

    public ForgeConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder().worldRestart();

        builder.comment("Booleans");

        toolsAreWeapons = defineBoolean(builder, "toolsAreWeapons", false);

        midairKB = defineBoolean(builder, "midairKB",false);

        fishingHookKB = defineBoolean(builder, "fishingHookKB",false);

		fistDamage = defineBoolean(builder, "fistDamage", false);

        swordBlocking = defineBoolean(builder, "swordBlocking",false);

		shieldOnlyWhenCharged = defineBoolean(builder, "shieldOnlyWhenCharged",false);

        sprintCritsEnabled = defineBoolean(builder, "sprintCritsEnabled",true);

        saturationHealing = defineBoolean(builder, "saturationHealing",false);

		fastHealing = defineBoolean(builder, "fastHealing",false);

		letVanillaConnect = defineBoolean(builder, "letVanillaConnect",false);

		oldSprintFoodRequirement = defineBoolean(builder, "oldSprintFoodRequirement",false);

		projectilesHaveIFrames = defineBoolean(builder, "projectilesHaveIFrames",false);

		magicHasIFrames = defineBoolean(builder, "magicHasIFrames",true);

        autoAttackAllowed = defineBoolean(builder, "autoAttackAllowed",true);

        configOnlyWeapons = defineBoolean(builder, "configOnlyWeapons",false, true);

		tieredShields = defineBoolean(builder, "tieredShields",false, true);

		piercer = defineBoolean(builder, "piercer",false, true);

		defender = defineBoolean(builder, "defender",false, true);

        attackReach = defineBoolean(builder, "attackReach", true);

        attackSpeed = defineBoolean(builder, "attackSpeed", true);

		instaAttack = defineBoolean(builder, "instaAttack",false);

        ctsAttackBalancing = defineBoolean(builder, "ctsAttackBalancing", true);

        eatingInterruption = defineBoolean(builder, "eatingInterruption", true);

		improvedMiscEntityAttacks = defineBoolean(builder, "improvedMiscEntityAttacks", false);

        builder.comment("Integers");

        swordProtectionEfficacy = defineIntRange(builder, "potionUseDuration", 0,-3,4);

        potionUseDuration = defineIntRange(builder, "potionUseDuration", 20,1,1000);

        honeyBottleUseDuration = defineIntRange(builder, "honeyBottleUseDuration",20,1,1000);

        milkBucketUseDuration = defineIntRange(builder, "milkBucketUseDuration",20,1,1000);

        stewUseDuration = defineIntRange(builder, "stewUseDuration",20,1,1000);

        instantHealthBonus = defineIntRange(builder, "instantHealthBonus", 6, 1,1000);

		shieldChargePercentage = defineIntRange(builder, "shieldChargePercentage", 195, 1,200);

        builder.comment("Doubles");

		shieldDisableTime = defineDoubleRange(builder, "shieldDisableTime",1.6,0,10);

		cleavingDisableTime = defineDoubleRange(builder, "cleavingDisableTime",0.5,0,10);

		defenderDisableReduction = defineDoubleRange(builder, "defenderDisableReduction",0.5,0,10);

        snowballDamage = defineDoubleRange(builder, "snowballDamage",0,0,40.0);

        eggDamage = defineDoubleRange(builder, "eggDamage",0,0,40.0);

        bowUncertainty = defineDoubleRange(builder, "bowUncertainty",0.25,0,4);

		baseHandAttackSpeed = defineDoubleRange(builder, "baseHandAttackSpeed",2.5,0,20);

		slowestToolAttackSpeed = defineDoubleRange(builder, "slowestToolAttackSpeed",-1,-1,17.5);

		slowToolAttackSpeed = defineDoubleRange(builder, "slowToolAttackSpeed",-0.5,-1,17.5);

		fastToolAttackSpeed = defineDoubleRange(builder, "fastToolAttackSpeed",0.5,-1,17.5);

		fastestToolAttackSpeed = defineDoubleRange(builder, "fastestToolAttackSpeed",1,-1,17.5);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,builder.build());
    }

	public BooleanOption defineBoolean(ForgeConfigSpec.Builder builder, String path, boolean defaultValue) {
		return defineBoolean(builder, path, defaultValue, false);
	}
	public BooleanOption defineBoolean(ForgeConfigSpec.Builder builder, String path, boolean defaultValue, boolean restartRequired) {
		ForgeConfigSpec.BooleanValue booleanValue = builder.define(path, defaultValue);
		BooleanOption option = new BooleanOption(booleanValue, restartRequired);
		options.put(path, option);
		return option;
	}
	public IntOption defineIntRange(ForgeConfigSpec.Builder builder, String path, int defaultValue, int min, int max) {
		return defineIntRange(builder, path, defaultValue, min, max, false);
	}
	public IntOption defineIntRange(ForgeConfigSpec.Builder builder, String path, int defaultValue, int min, int max, boolean restartRequired) {
		ForgeConfigSpec.IntValue intValue = builder.defineInRange(path, defaultValue, min, max);
		IntOption option = new IntOption(intValue, restartRequired);
		options.put(path, option);
		return option;
	}
	public DoubleOption defineDoubleRange(ForgeConfigSpec.Builder builder, String path, double defaultValue, double min, double max) {
		return defineDoubleRange(builder, path, defaultValue, min, max, false);
	}
	public DoubleOption defineDoubleRange(ForgeConfigSpec.Builder builder, String path, double defaultValue, double min, double max, boolean restartRequired) {
		ForgeConfigSpec.DoubleValue doubleValue = builder.defineInRange(path, defaultValue, min, max);
		DoubleOption option = new DoubleOption(doubleValue, restartRequired);
		options.put(path, option);
		return option;
	}

}
