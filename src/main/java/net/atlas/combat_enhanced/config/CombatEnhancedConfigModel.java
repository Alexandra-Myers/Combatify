package net.atlas.combat_enhanced.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@SuppressWarnings("unused")
@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Modmenu(modId = "combat_enhanced")
@Config(name = "combat-enhanced-config", wrapperName = "CombatEnhancedConfig")
public class CombatEnhancedConfigModel {
	@SectionHeader("Booleans")
	public boolean toolsAreWeapons = false;
	public boolean refinedCoyoteTime = false;
	public boolean midairKB = false;
	public boolean fishingHookKB = false;
	public boolean fistDamage = false;
	public boolean swordBlocking = false;
	public boolean shieldOnlyWhenCharged = false;
	public boolean sprintCritsEnabled = true;
	public boolean saturationHealing = false;
	public boolean fastHealing = false;
	public boolean projectilesHaveIFrames = false;
	public boolean magicHasIFrames = true;
	public boolean autoAttackAllowed = true;
	@RestartRequired
	public boolean configOnlyWeapons = false;
	@RestartRequired
	public boolean piercer = false;
	@RestartRequired
	public boolean defender = false;
	public boolean attackReach = true;
	public boolean attackSpeed = true;
	public boolean ctsAttackBalancing = true;
	public boolean eatingInterruption = true;
	@SectionHeader("Integers")
	@RangeConstraint(min = -3, max = 4)
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
	@RangeConstraint(min = 1, max = 1000)
	public int eggItemCooldown = 4;
	@RangeConstraint(min = 1, max = 1000)
	public int snowballItemCooldown = 4;
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
	@RangeConstraint(min = 0, max = 1000)
	public float swordAttackDamage = 1;
	@RangeConstraint(min = 0, max = 1000)
	public float axeAttackDamage = 2;
	@RangeConstraint(min = 0, max = 1000)
	public float knifeAttackDamage = 0;
	@RangeConstraint(min = 0, max = 1000)
	public float baseLongswordAttackDamage = 0;
	@RangeConstraint(min = 0, max = 1000)
	public float ironDiaLongswordAttackDamage = 1;
	@RangeConstraint(min = 0, max = 1000)
	public float netheriteLongswordAttackDamage = 2;
	@RangeConstraint(min = 0, max = 1000)
	public float baseHoeAttackDamage = 0;
	@RangeConstraint(min = 0, max = 1000)
	public float ironDiaHoeAttackDamage = 1;
	@RangeConstraint(min = 0, max = 1000)
	public float netheriteHoeAttackDamage = 2;
	@RangeConstraint(min = 0, max = 1000)
	public float tridentAttackDamage = 5;
	@RangeConstraint(min = -1, max = 7.5)
	public float longswordAttackSpeed = 0.5F;
	@RangeConstraint(min = -1, max = 7.5)
	public float swordAttackSpeed = 0.5F;
	@RangeConstraint(min = -1, max = 7.5)
	public float axeAttackSpeed = -0.5F;
	@RangeConstraint(min = -1, max = 7.5)
	public float woodenHoeAttackSpeed = -0.5F;
	@RangeConstraint(min = -1, max = 7.5)
	public float stoneHoeAttackSpeed = 0;
	@RangeConstraint(min = -1, max = 7.5)
	public float ironHoeAttackSpeed = 0.5F;
	@RangeConstraint(min = -1, max = 7.5)
	public float goldDiaNethHoeAttackSpeed = 1;
	@RangeConstraint(min = -1, max = 7.5)
	public float knifeAttackSpeed = 1;
	@RangeConstraint(min = -1, max = 7.5)
	public float tridentAttackSpeed = -0.5F;
	@RangeConstraint(min = -1, max = 7.5)
	public float defaultAttackSpeed = 0F;
	@RangeConstraint(min = -1, max = 7.5)
	public float slowestToolAttackSpeed = -1F;
	@RangeConstraint(min = -1, max = 7.5)
	public float slowToolAttackSpeed = -0.5F;
	@RangeConstraint(min = -1, max = 7.5)
	public float fastToolAttackSpeed = 0.5F;
	@RangeConstraint(min = -1, max = 7.5)
	public float fastestToolAttackSpeed = 1F;
	@RangeConstraint(min = -1, max = 100)
	public float swordAttackReach = 0.5F;
	@RangeConstraint(min = -1, max = 100)
	public float axeAttackReach = 0F;
	@RangeConstraint(min = -1, max = 100)
	public float hoeAttackReach = 1;
	@RangeConstraint(min = -1, max = 100)
	public float longswordAttackReach = 1;
	@RangeConstraint(min = -1, max = 100)
	public float knifeAttackReach = 0F;
	@RangeConstraint(min = -1, max = 100)
	public float tridentAttackReach = 1;
	@RangeConstraint(min = -1, max = 100)
	public float defaultAttackReach = 0F;
	public static class UseDurations {
	}
	public static class Cooldowns {
	}
}
