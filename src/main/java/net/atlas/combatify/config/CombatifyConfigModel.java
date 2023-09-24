package net.atlas.combatify.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@SuppressWarnings("unused")
@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Modmenu(modId = "combatify")
@Config(name = "combatify-config", wrapperName = "CombatifyConfig")
public class CombatifyConfigModel {
	@SectionHeader("Booleans")
	public boolean toolsAreWeapons = false;
	public boolean midairKB = false;
	public boolean fishingHookKB = false;
	public boolean fistDamage = false;
	public boolean swordBlocking = false;
	public boolean shieldOnlyWhenCharged = false;
	public boolean sprintCritsEnabled = true;
	public boolean saturationHealing = false;
	public boolean fastHealing = false;
	public boolean letVanillaConnect = false;
	public boolean oldSprintFoodRequirement = false;
	public boolean projectilesHaveIFrames = false;
	public boolean magicHasIFrames = true;
	public boolean autoAttackAllowed = true;
	@RestartRequired
	public boolean configOnlyWeapons = false;
	@RestartRequired
	public boolean tieredShields = false;
	@RestartRequired
	public boolean piercer = false;
	@RestartRequired
	public boolean defender = false;
	public boolean attackReach = true;
	public boolean attackSpeed = true;
	public boolean instaAttack = false;
	public boolean ctsAttackBalancing = true;
	public boolean eatingInterruption = true;
	public boolean improvedMiscEntityAttacks = false;
	@SectionHeader("Integers")
	@RangeConstraint(min = -3, max = 7)
	public int swordProtectionEfficacy = 0;
	@RangeConstraint(min = 1, max = 1000)
	public int potionUseDuration = 20;
	@RangeConstraint(min = 1, max = 1000)
	public int honeyBottleUseDuration = 20;
	@RangeConstraint(min = 1, max = 1000)
	public int milkBucketUseDuration = 20;
	@RangeConstraint(min = 1, max = 1000)
	public int stewUseDuration = 20;
	@RangeConstraint(min = 1, max = 1000)
	public int instantHealthBonus = 6;
	@RangeConstraint(min = 1, max = 200)
	public int shieldChargePercentage = 195;
	@SectionHeader("Floats")
	@RangeConstraint(min = 0, max = 10)
	public float shieldDisableTime = 1.6F;
	@RangeConstraint(min = 0, max = 10)
	public float cleavingDisableTime = 0.5F;
	@RangeConstraint(min = 0, max = 10)
	public float defenderDisableReduction = 0.5F;
	@RangeConstraint(min = 0, max = 40)
	public float snowballDamage = 0.0F;
	@RangeConstraint(min = 0, max = 40)
	public float eggDamage = 0.0F;
	@RangeConstraint(min = 0, max = 4)
	public float bowUncertainty = 0.25F;
	@RangeConstraint(min = 2.5, max = 20)
	public float baseHandAttackSpeed = 2.5F;
	@RangeConstraint(min = -1, max = 17.5F)
	public float slowestToolAttackSpeed = -1F;
	@RangeConstraint(min = -1, max = 17.5F)
	public float slowToolAttackSpeed = -0.5F;
	@RangeConstraint(min = -1, max = 17.5F)
	public float fastToolAttackSpeed = 0.5F;
	@RangeConstraint(min = -1, max = 17.5F)
	public float fastestToolAttackSpeed = 1F;
	@RangeConstraint(min = 0, max = 5)
	public float minHitboxSize = 0.9F;
	public static class UseDurations {
	}
	public static class Cooldowns {
	}
}
