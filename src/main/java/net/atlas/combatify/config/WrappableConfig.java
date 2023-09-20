package net.atlas.combatify.config;

import eu.midnightdust.lib.config.MidnightConfig;

@SuppressWarnings("unused")
public class WrappableConfig extends MidnightConfig {

	@Comment(centered = true) public static Comment Booleans;
	@Entry public static boolean toolsAreWeapons = false;
	@Entry public static boolean midairKB = false;
	@Entry public static boolean fishingHookKB = false;
	@Entry public static boolean fistDamage = false;
	@Entry public static boolean swordBlocking = false;
	@Entry public static boolean shieldOnlyWhenCharged = false;
	@Entry public static boolean sprintCritsEnabled = true;
	@Entry public static boolean saturationHealing = false;
	@Entry public static boolean fastHealing = false;
	@Entry public static boolean letVanillaConnect = false;
	@Entry public static boolean oldSprintFoodRequirement = false;
	@Entry public static boolean projectilesHaveIFrames = false;
	@Entry public static boolean magicHasIFrames = true;
	@Entry public static boolean autoAttackAllowed = true;
	@Entry public static boolean configOnlyWeapons = false;
	@Entry public static boolean tieredShields = false;
	@Entry public static boolean piercer = false;
	@Entry public static boolean defender = false;
	@Entry public static boolean attackReach = true;
	@Entry public static boolean attackSpeed = true;
	@Entry public static boolean instaAttack = false;
	@Entry public static boolean ctsAttackBalancing = true;
	@Entry public static boolean eatingInterruption = true;
	@Entry public static boolean improvedMiscEntityAttacks = false;
	@Comment(centered = true) public static Comment Integers;
	@Entry(isSlider = true, min = -3, max = 4) public static int swordProtectionEfficacy = 0;
	@Entry(isSlider = true, min = 1, max = 1000) public static int potionUseDuration = 20;
	@Entry(isSlider = true, min = 1, max = 1000) public static int honeyBottleUseDuration = 20;
	@Entry(isSlider = true, min = 1, max = 1000) public static int milkBucketUseDuration = 20;
	@Entry(isSlider = true, min = 1, max = 1000) public static int stewUseDuration = 20;
	@Entry(isSlider = true, min = 1, max = 1000) public static int instantHealthBonus = 6;
	@Entry(isSlider = true, min = 1, max = 200) public static int shieldChargePercentage = 195;
	@Comment(centered = true) public static Comment Doubles;
	@Entry(isSlider = true, min = 0, max = 10) public static double shieldDisableTime = 1.6F;
	@Entry(isSlider = true, min = 0, max = 10) public static double cleavingDisableTime = 0.5F;
	@Entry(isSlider = true, min = 0, max = 10) public static double defenderDisableReduction = 0.5F;
	@Entry(isSlider = true, min = 0, max = 40) public static double snowballDamage = 0;
	@Entry(isSlider = true, min = 0, max = 40) public static double eggDamage = 0;
	@Entry(isSlider = true, min = 0, max = 4) public static double bowUncertainty = 0.25F;
	@Entry(isSlider = true, min = 0, max = 20) public static double baseHandAttackSpeed = 2.5F;
	@Entry(isSlider = true, min = -1, max = 17.5) public static double slowestToolAttackSpeed = -1;
	@Entry(isSlider = true, min = -1, max = 17.5) public static double slowToolAttackSpeed = -0.5F;
	@Entry(isSlider = true, min = -1, max = 17.5) public static double fastToolAttackSpeed = 0.5F;
	@Entry(isSlider = true, min = -1, max = 17.5) public static double fastestToolAttackSpeed = 1;

}
