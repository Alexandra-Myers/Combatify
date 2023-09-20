package net.atlas.combatify.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

import java.lang.reflect.Field;

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
		MidnightConfig.init("combatify", WrappableConfig.class);

        toolsAreWeapons = defineBoolean(WrappableConfig.class, "toolsAreWeapons");

        midairKB = defineBoolean(WrappableConfig.class, "midairKB");

        fishingHookKB = defineBoolean(WrappableConfig.class, "fishingHookKB");

		fistDamage = defineBoolean(WrappableConfig.class, "fistDamage");

        swordBlocking = defineBoolean(WrappableConfig.class, "swordBlocking");

		shieldOnlyWhenCharged = defineBoolean(WrappableConfig.class, "shieldOnlyWhenCharged");

        sprintCritsEnabled = defineBoolean(WrappableConfig.class, "sprintCritsEnabled");

        saturationHealing = defineBoolean(WrappableConfig.class, "saturationHealing");

		fastHealing = defineBoolean(WrappableConfig.class, "fastHealing");

		letVanillaConnect = defineBoolean(WrappableConfig.class, "letVanillaConnect");

		oldSprintFoodRequirement = defineBoolean(WrappableConfig.class, "oldSprintFoodRequirement");

		projectilesHaveIFrames = defineBoolean(WrappableConfig.class, "projectilesHaveIFrames");

		magicHasIFrames = defineBoolean(WrappableConfig.class, "magicHasIFrames");

        autoAttackAllowed = defineBoolean(WrappableConfig.class, "autoAttackAllowed");

        configOnlyWeapons = defineBoolean(WrappableConfig.class, "configOnlyWeapons", true);

		tieredShields = defineBoolean(WrappableConfig.class, "tieredShields", true);

		piercer = defineBoolean(WrappableConfig.class, "piercer", true);

		defender = defineBoolean(WrappableConfig.class, "defender", true);

        attackReach = defineBoolean(WrappableConfig.class, "attackReach");

        attackSpeed = defineBoolean(WrappableConfig.class, "attackSpeed");

		instaAttack = defineBoolean(WrappableConfig.class, "instaAttack");

        ctsAttackBalancing = defineBoolean(WrappableConfig.class, "ctsAttackBalancing");

        eatingInterruption = defineBoolean(WrappableConfig.class, "eatingInterruption");

		improvedMiscEntityAttacks = defineBoolean(WrappableConfig.class, "improvedMiscEntityAttacks");

        swordProtectionEfficacy = defineIntRange(WrappableConfig.class, "swordProtectionEfficacy");

        potionUseDuration = defineIntRange(WrappableConfig.class, "potionUseDuration");

        honeyBottleUseDuration = defineIntRange(WrappableConfig.class, "honeyBottleUseDuration");

        milkBucketUseDuration = defineIntRange(WrappableConfig.class, "milkBucketUseDuration");

        stewUseDuration = defineIntRange(WrappableConfig.class, "stewUseDuration");

        instantHealthBonus = defineIntRange(WrappableConfig.class, "instantHealthBonus");

		shieldChargePercentage = defineIntRange(WrappableConfig.class, "shieldChargePercentage");

		shieldDisableTime = defineDoubleRange(WrappableConfig.class, "shieldDisableTime");

		cleavingDisableTime = defineDoubleRange(WrappableConfig.class, "cleavingDisableTime");

		defenderDisableReduction = defineDoubleRange(WrappableConfig.class, "defenderDisableReduction");

        snowballDamage = defineDoubleRange(WrappableConfig.class, "snowballDamage");

        eggDamage = defineDoubleRange(WrappableConfig.class, "eggDamage");

        bowUncertainty = defineDoubleRange(WrappableConfig.class, "bowUncertainty");

		baseHandAttackSpeed = defineDoubleRange(WrappableConfig.class, "baseHandAttackSpeed");

		slowestToolAttackSpeed = defineDoubleRange(WrappableConfig.class, "slowestToolAttackSpeed");

		slowToolAttackSpeed = defineDoubleRange(WrappableConfig.class, "slowToolAttackSpeed");

		fastToolAttackSpeed = defineDoubleRange(WrappableConfig.class, "fastToolAttackSpeed");

		fastestToolAttackSpeed = defineDoubleRange(WrappableConfig.class, "fastestToolAttackSpeed");
    }

	public BooleanOption defineBoolean(Class<? extends MidnightConfig> builder, String path) {
		return defineBoolean(builder, path, false);
	}
	public BooleanOption defineBoolean(Class<? extends MidnightConfig> builder, String path, boolean restartRequired) {
		try {
			Field booleanValue = builder.getField(path);
			if (!booleanValue.isAnnotationPresent(MidnightConfig.Entry.class) || !(booleanValue.getType() == boolean.class || booleanValue.getType() == Boolean.class))
				throw new ReportedException(new CrashReport("Tried to access an invalid config option!", new NoSuchFieldException()));
			BooleanOption option = new BooleanOption(booleanValue, restartRequired);
			options.put(path, option);
			return option;
		} catch (NoSuchFieldException | SecurityException e) {
			throw new ReportedException(new CrashReport("Tried to access an invalid config option!", e));
		}
	}
	public IntOption defineIntRange(Class<? extends MidnightConfig> builder, String path) {
		return defineIntRange(builder, path, false);
	}
	public IntOption defineIntRange(Class<? extends MidnightConfig> builder, String path, boolean restartRequired) {
		try {
			Field intValue = builder.getField(path);
			if (!intValue.isAnnotationPresent(MidnightConfig.Entry.class) || !(intValue.getType() == int.class || intValue.getType() == Integer.class))
				throw new ReportedException(new CrashReport("Tried to access an invalid config option!", new NoSuchFieldException()));
			IntOption option = new IntOption(intValue, restartRequired);
			options.put(path, option);
			return option;
		} catch (NoSuchFieldException | SecurityException e) {
			throw new ReportedException(new CrashReport("Tried to access an invalid config option!", e));
		}
	}
	public DoubleOption defineDoubleRange(Class<? extends MidnightConfig> builder, String path) {
		return defineDoubleRange(builder, path, false);
	}
	public DoubleOption defineDoubleRange(Class<? extends MidnightConfig> builder, String path, boolean restartRequired) {
		try {
			Field doubleValue = builder.getField(path);
			if (!doubleValue.isAnnotationPresent(MidnightConfig.Entry.class) || !(doubleValue.getType() == double.class || doubleValue.getType() == Double.class))
				throw new ReportedException(new CrashReport("Tried to access an invalid config option!", new NoSuchFieldException()));
			DoubleOption option = new DoubleOption(doubleValue, restartRequired);
			options.put(path, option);
			return option;
		} catch (NoSuchFieldException | SecurityException e) {
			throw new ReportedException(new CrashReport("Tried to access an invalid config option!", e));
		}
	}

}
