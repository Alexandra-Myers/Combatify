package net.atlas.combatify.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class WrappableConfig extends MidnightConfig {

	@Comment(category = "text", centered = true) public static Comment Booleans;
	@Entry(category = "text") public static boolean toolsAreWeapons = false;
	@Entry(category = "text") public static boolean midairKB = false;
	@Entry(category = "text") public static boolean fishingHookKB = false;
	@Entry(category = "text") public static boolean fistDamage = false;
	@Entry(category = "text") public static boolean swordBlocking = false;
	@Entry(category = "text") public static boolean shieldOnlyWhenCharged = false;
	@Entry(category = "text") public static boolean sprintCritsEnabled = true;
	@Entry(category = "text") public static boolean saturationHealing = false;
	@Entry(category = "text") public static boolean fastHealing = false;
	@Entry(category = "text") public static boolean letVanillaConnect = false;
	@Entry(category = "text") public static boolean oldSprintFoodRequirement = false;
	@Entry(category = "text") public static boolean projectilesHaveIFrames = false;
	@Entry(category = "text") public static boolean magicHasIFrames = true;
	@Entry(category = "text") public static boolean autoAttackAllowed = true;
	@Entry(category = "text") public static boolean configOnlyWeapons = false;
	@Entry(category = "text") public static boolean tieredShields = false;
	@Entry(category = "text") public static boolean piercer = false;
	@Entry(category = "text") public static boolean defender = false;
	@Entry(category = "text") public static boolean attackReach = true;
	@Entry(category = "text") public static boolean attackSpeed = true;
	@Entry(category = "text") public static boolean instaAttack = false;
	@Entry(category = "text") public static boolean ctsAttackBalancing = true;
	@Entry(category = "text") public static boolean eatingInterruption = true;
	@Entry(category = "text") public static boolean improvedMiscEntityAttacks = false;
	@Comment(category = "text", centered = true) public static Comment Integers;
	@Entry(category = "text", isSlider = true, min = -3, max = 4) public static int swordProtectionEfficacy = 0;
	@Entry(category = "text", isSlider = true, min = 1, max = 1000) public static int potionUseDuration = 20;
	@Entry(category = "text", isSlider = true, min = 1, max = 1000) public static int honeyBottleUseDuration = 20;
	@Entry(category = "text", isSlider = true, min = 1, max = 1000) public static int milkBucketUseDuration = 20;
	@Entry(category = "text", isSlider = true, min = 1, max = 1000) public static int stewUseDuration = 20;
	@Entry(category = "text", isSlider = true, min = 1, max = 1000) public static int instantHealthBonus = 6;
	@Entry(category = "text", isSlider = true, min = 1, max = 200) public static int shieldChargePercentage = 195;
	@Comment(category = "text", centered = true) public static Comment Doubles;
	@Entry(category = "text", isSlider = true, min = 0, max = 10) public static double shieldDisableTime = 1.6F;
	@Entry(category = "text", isSlider = true, min = 0, max = 10) public static double cleavingDisableTime = 0.5F;
	@Entry(category = "text", isSlider = true, min = 0, max = 10) public static double defenderDisableReduction = 0.5F;
	@Entry(category = "text", isSlider = true, min = 0, max = 40) public static double snowballDamage = 0;
	@Entry(category = "text", isSlider = true, min = 0, max = 40) public static double eggDamage = 0;
	@Entry(category = "text", isSlider = true, min = 0, max = 4) public static double bowUncertainty = 0.25F;
	@Entry(category = "text", isSlider = true, min = 0, max = 20) public static double baseHandAttackSpeed = 2.5F;
	@Entry(category = "text", isSlider = true, min = -1, max = 17.5) public static double slowestToolAttackSpeed = -1;
	@Entry(category = "text", isSlider = true, min = -1, max = 17.5) public static double slowToolAttackSpeed = -0.5F;
	@Entry(category = "text", isSlider = true, min = -1, max = 17.5) public static double fastToolAttackSpeed = 0.5F;
	@Entry(category = "text", isSlider = true, min = -1, max = 17.5) public static double fastestToolAttackSpeed = 1;

}
