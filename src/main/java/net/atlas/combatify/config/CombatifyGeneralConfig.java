package net.atlas.combatify.config;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import net.atlas.atlascore.AtlasCore;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.atlascore.util.Codecs;
import net.atlas.atlascore.util.ConfigRepresentable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.atlas.combatify.Combatify.*;

public class CombatifyGeneralConfig extends AtlasConfig {
	private BooleanHolder iFramesBasedOnWeapon;
	private BooleanHolder bowFatigue;
	private BooleanHolder bedrockBridging;
	private BooleanHolder chargedAttacks;
	private BooleanHolder canAttackEarly;
	private BooleanHolder canSweepOnMiss;
	private BooleanHolder chargedReach;
	private BooleanHolder creativeAttackReach;
	private BooleanHolder missedAttackRecovery;
	private BooleanHolder hasMissTime;
	private BooleanHolder canInteractWhenCrouchShield;
	private BooleanHolder bedrockImpaling;
	private BooleanHolder dispensableTridents;
	private BooleanHolder disableLoyaltyOnHitEntity;
	private BooleanHolder snowballKB;
	private BooleanHolder resetOnItemChange;
	private BooleanHolder sweepWithSweeping;
	private BooleanHolder sweepConditionsMatchMiss;
	private BooleanHolder sweepingNegatedForTamed;
	private BooleanHolder ctsMomentumPassedToProjectiles;
	private BooleanHolder swingThroughGrass;
	private BooleanHolder delayedEntityUpdates;
	private BooleanHolder strengthAppliesToEnchants;
	private BooleanHolder percentageDamageEffects;
	private BooleanHolder tierDamageNerf;
	private BooleanHolder tridentVoidReturn;
	private BooleanHolder fishingHookKB;
	private BooleanHolder shieldOnlyWhenCharged;
	private BooleanHolder letVanillaConnect;
	private BooleanHolder projectilesHaveIFrames;
	private BooleanHolder magicHasIFrames;
	private BooleanHolder autoAttackAllowed;
	private BooleanHolder configOnlyWeapons;
	private BooleanHolder tieredShields;
	private BooleanHolder attackReach;
	private BooleanHolder armorPiercingDisablesShields;
	private BooleanHolder attackSpeed;
	private BooleanHolder instaAttack;
	private BooleanHolder improvedMiscEntityAttacks;
	private BooleanHolder hasteFix;
	private BooleanHolder enableDebugLogging;
	private BooleanHolder mobsCanGuard;
	private BooleanHolder mobsCanSprint;
	private BooleanHolder mobsUsePlayerAttributes;
	private IntegerHolder shieldDelay;
	private IntegerHolder instantHealthBonus;
	private IntegerHolder shieldChargePercentage;
	private DoubleHolder fistDamage;
	private DoubleHolder fallbackShieldDisableTime;
	private DoubleHolder baseHandAttackSpeed;
	private DoubleHolder minHitboxSize;
	private EnumHolder<EatingInterruptionMode> eatingInterruptionMode;
	private EnumHolder<KnockbackMode> knockbackMode;
	private EnumHolder<ArrowDisableMode> arrowDisableMode;
	private EnumHolder<ArmourPiercingMode> armourPiercingMode;
	private ObjectHolder<AttackDecay> attackDecay;
	private ObjectHolder<ProjectileUncertainty> projectileUncertainty;
	private ObjectHolder<ProjectileDamage> projectileDamage;
	private TagHolder<JSImpl> critImpl;
	private TagHolder<JSImpl> foodImpl;
	private Category ctsB;
	private Category ctsI;
	private Category ctsD;
	private Category ctsE;
	private Category extraB;
	private Category extraI;
	private Category extraD;
	private Category extraE;
	private Category debug;

	public CombatifyGeneralConfig() {
		this(id("combatify-general"));
		declareDefaultForMod("combatify");
	}

	public CombatifyGeneralConfig(ResourceLocation id) {
		super(id);
	}

	@Override
	public Component getFormattedName() {
		return Component.translatable("text.config." + this.name.getPath() + ".title").withStyle(Style.EMPTY.withColor(AtlasCore.CONFIG.configNameDisplayColour.get()));
	}

	@Override
	public void handleExtraSync(AtlasCore.AtlasConfigPacket atlasConfigPacket, ClientPlayNetworking.Context context) {

	}

	@Override
	public void handleConfigInformation(AtlasCore.ClientInformPacket clientInformPacket, ServerPlayer serverPlayer, PacketSender packetSender) {

	}

	@Override
	public void defineConfigHolders() {
		attackDecay = createObject("attackDecay", AttackDecay.DEFAULT, AttackDecay.class, AttackDecay.STREAM_CODEC, false);
		attackDecay.tieToCategory(ctsB);
		attackReach = createBoolean("attackReach", true);
		attackReach.tieToCategory(ctsB);
		attackReach.setupTooltip(1);
		autoAttackAllowed = createBoolean("autoAttackAllowed", true);
		autoAttackAllowed.tieToCategory(ctsB);
		autoAttackAllowed.setupTooltip(1);
		bedrockBridging = createBoolean("bedrockBridging", false);
		bedrockBridging.tieToCategory(ctsB);
		bedrockBridging.setupTooltip(1);
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
		creativeAttackReach = createBoolean("creativeAttackReach", false);
		creativeAttackReach.tieToCategory(ctsB);
		creativeAttackReach.setupTooltip(1);
		ctsMomentumPassedToProjectiles = createBoolean("ctsMomentumPassedToProjectiles", true);
		ctsMomentumPassedToProjectiles.tieToCategory(ctsB);
		ctsMomentumPassedToProjectiles.setupTooltip(1);
		dispensableTridents = createBoolean("dispensableTridents", true);
		dispensableTridents.tieToCategory(ctsB);
		dispensableTridents.setupTooltip(1);
		hasMissTime = createBoolean("hasMissTime", false);
		hasMissTime.tieToCategory(ctsB);
		hasMissTime.setupTooltip(1);
		hasteFix = createBoolean("hasteFix", false);
		hasteFix.tieToCategory(ctsB);
		hasteFix.setupTooltip(1);
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
		snowballKB = createBoolean("snowballKB", true);
		snowballKB.tieToCategory(ctsB);
		snowballKB.setupTooltip(1);
		strengthAppliesToEnchants = createBoolean("strengthAppliesToEnchants", true);
		strengthAppliesToEnchants.tieToCategory(ctsB);
		strengthAppliesToEnchants.setupTooltip(1);
		sweepWithSweeping = createBoolean("sweepWithSweeping", true);
		sweepWithSweeping.tieToCategory(ctsB);
		sweepWithSweeping.setupTooltip(1);
		swingThroughGrass = createBoolean("swingThroughGrass", true);
		swingThroughGrass.tieToCategory(ctsB);
		swingThroughGrass.setupTooltip(1);
		tierDamageNerf = createBoolean("tierDamageNerf", true);
		tierDamageNerf.tieToCategory(ctsB);
		tierDamageNerf.setupTooltip(1);
		tridentVoidReturn = createBoolean("tridentVoidReturn", true);
		tridentVoidReturn.tieToCategory(ctsB);
		tridentVoidReturn.setupTooltip(1);

		shieldDelay = createInRange("shieldDelay", 0, 0, 2000, false);
		shieldDelay.tieToCategory(ctsI);
		shieldDelay.setupTooltip(1);
		instantHealthBonus = createInRange("instantHealthBonus", 6, 1, 1000, false);
		instantHealthBonus.tieToCategory(ctsI);

		baseHandAttackSpeed = createInRange("baseHandAttackSpeed", 2.5, 2.5, 20);
		baseHandAttackSpeed.tieToCategory(ctsD);
		fistDamage = createInRange("fistDamage", 2.0, 1, 1024);
		fistDamage.tieToCategory(ctsD);
		fistDamage.setupTooltip(1);
		projectileUncertainty = createObject("projectileUncertainty", ProjectileUncertainty.DEFAULT, ProjectileUncertainty.class, ProjectileUncertainty.STREAM_CODEC, false);
		projectileUncertainty.tieToCategory(ctsD);
		fallbackShieldDisableTime = createInRange("fallbackShieldDisableTime", 1.6, 0, 10);
		fallbackShieldDisableTime.tieToCategory(ctsD);
		fallbackShieldDisableTime.setupTooltip(1);
		minHitboxSize = createInRange("minHitboxSize", 0.9, 0, 5);
		minHitboxSize.tieToCategory(ctsD);
		minHitboxSize.setupTooltip(1);

		critImpl = createCodecBacked("critImpl", new JSImpl("cts_crit_impl"), JSImpl.CODEC);
		critImpl.tieToCategory(ctsE);
		critImpl.setupTooltip(1);
		eatingInterruptionMode = createEnum("eatingInterruptionMode", EatingInterruptionMode.FULL_RESET, EatingInterruptionMode.class, EatingInterruptionMode.values(), e -> Component.translatable("text.config.combatify-general.option.eatingInterruptionMode." + e.name().toLowerCase(Locale.ROOT)));
		eatingInterruptionMode.tieToCategory(ctsE);
		eatingInterruptionMode.setupTooltip(4);
		knockbackMode = createEnum("knockbackMode", KnockbackMode.CTS_8C, KnockbackMode.class, KnockbackMode.values(), e -> Component.translatable("text.config.combatify-general.option.knockbackMode." + e.name().toLowerCase(Locale.ROOT)));
		knockbackMode.tieToCategory(ctsB);
		knockbackMode.setupTooltip(6);
		foodImpl = createCodecBacked("foodImpl", new JSImpl("cts_food_impl"), JSImpl.CODEC);
		foodImpl.tieToCategory(ctsE);
		foodImpl.setupTooltip(1);

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
		configOnlyWeapons.setRestartRequired(RestartRequiredMode.RESTART_BOTH);
		configOnlyWeapons.setupTooltip(1);
		tieredShields = createBoolean("tieredShields", false);
		tieredShields.tieToCategory(extraB);
		tieredShields.setRestartRequired(RestartRequiredMode.RESTART_BOTH);
		tieredShields.setupTooltip(1);
		delayedEntityUpdates = createBoolean("delayedEntityUpdates", false);
		delayedEntityUpdates.tieToCategory(extraB);
		delayedEntityUpdates.setupTooltip(1);
		disableLoyaltyOnHitEntity = createBoolean("disableLoyaltyOnHitEntity", false);
		disableLoyaltyOnHitEntity.tieToCategory(extraB);
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
		mobsCanGuard = createBoolean("mobsCanGuard", false);
		mobsCanGuard.tieToCategory(extraB);
		mobsCanGuard.setupTooltip(1);
		mobsCanSprint = createBoolean("mobsCanSprint", false);
		mobsCanSprint.tieToCategory(extraB);
		mobsCanSprint.setupTooltip(1);
		mobsUsePlayerAttributes = createBoolean("mobsUsePlayerAttributes", false);
		mobsUsePlayerAttributes.tieToCategory(extraB);
		mobsUsePlayerAttributes.setupTooltip(1);
		shieldOnlyWhenCharged = createBoolean("shieldOnlyWhenCharged", false);
		shieldOnlyWhenCharged.tieToCategory(extraB);
		shieldOnlyWhenCharged.setupTooltip(2);
		sweepConditionsMatchMiss = createBoolean("sweepConditionsMatchMiss", false);
		sweepConditionsMatchMiss.tieToCategory(extraB);
		sweepConditionsMatchMiss.setupTooltip(2);
		sweepingNegatedForTamed = createBoolean("sweepingNegatedForTamed", false);
		sweepingNegatedForTamed.tieToCategory(extraB);
		sweepingNegatedForTamed.setupTooltip(1);

		shieldChargePercentage = createInRange("shieldChargePercentage", 195, 1, 200, true);
		shieldChargePercentage.tieToCategory(extraI);
		shieldChargePercentage.setupTooltip(1);

		projectileDamage = createObject("projectileDamage", ProjectileDamage.DEFAULT, ProjectileDamage.class, ProjectileDamage.STREAM_CODEC);
		projectileDamage.tieToCategory(extraD);

		arrowDisableMode = createEnum("arrowDisableMode", ArrowDisableMode.NONE, ArrowDisableMode.class, ArrowDisableMode.values(), e -> Component.translatable("text.config.combatify-general.option.arrowDisableMode." + e.name().toLowerCase(Locale.ROOT)));
		arrowDisableMode.tieToCategory(extraE);
		arrowDisableMode.setupTooltip(7);
		armourPiercingMode = createEnum("armourPiercingMode", ArmourPiercingMode.VANILLA, ArmourPiercingMode.class, ArmourPiercingMode.values(), e -> Component.translatable("text.config.combatify-general.option.armourPiercingMode." + e.name().toLowerCase(Locale.ROOT)));
		armourPiercingMode.tieToCategory(extraE);
		armourPiercingMode.setupTooltip(4);

		enableDebugLogging = createBoolean("enableDebugLogging", false);
		enableDebugLogging.tieToCategory(debug);
	}

	@Override
	public @NotNull List<Category> createCategories() {
		List<Category> categoryList = super.createCategories();
		ctsB = new Category(this, "cts_booleans", new ArrayList<>());
		ctsI = new Category(this, "cts_integers", new ArrayList<>());
		ctsD = new Category(this, "cts_doubles", new ArrayList<>());
		ctsE = new Category(this, "cts_enums", new ArrayList<>());
		extraB = new Category(this, "extra_booleans", new ArrayList<>());
		extraI = new Category(this, "extra_integers", new ArrayList<>());
		extraD = new Category(this, "extra_doubles", new ArrayList<>());
		extraE = new Category(this, "extra_enums", new ArrayList<>());
		debug = new Category(this, "debug_options", new ArrayList<>());
		categoryList.add(ctsB);
		categoryList.add(ctsI);
		categoryList.add(ctsD);
		categoryList.add(ctsE);
		categoryList.add(extraB);
		categoryList.add(extraI);
		categoryList.add(extraD);
		categoryList.add(extraE);
		categoryList.add(debug);
		return categoryList;
	}

	@Override
	public void resetExtraHolders() {

	}

	@Override
	public <T> void alertChange(ConfigValue<T> tConfigValue, T newValue) {
		switch (newValue) {
			case Boolean bool when tConfigValue.name().equals("percentageDamageEffects") -> {
				if (isLoaded) {
					if (bool) {
						MobEffects.STRENGTH.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("effect.strength"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
						MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("effect.weakness"), -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
					} else {
						MobEffects.STRENGTH.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("effect.strength"), 3.0, AttributeModifier.Operation.ADD_VALUE);
						MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("effect.weakness"), -4.0, AttributeModifier.Operation.ADD_VALUE);
					}
				}
			}
			case Boolean bool when tConfigValue.name().equals("dispensableTridents") -> {
				if (isLoaded) {
					if (bool) DispenserBlock.registerProjectileBehavior(Items.TRIDENT);
					else DispenserBlock.registerBehavior(Items.TRIDENT, ((Object2ObjectOpenHashMap<Item, DispenseItemBehavior>) DispenserBlock.DISPENSER_REGISTRY).defaultReturnValue());
				}
			}
			case Boolean ignored when tConfigValue.name().equals("mobsCanGuard") -> {
				if (isLoaded) {
					mobConfigIsDirty = true;
				}
			}
            case null, default -> {

			}
        }
	}

	@Override
	public <T> void alertClientValue(ConfigValue<T> configValue, T t, T t1) {

	}

	@Override
	protected void loadExtra(JsonObject jsonObject) {

	}

	@Override
	@Environment(EnvType.CLIENT)
	public Screen createScreen(Screen prevScreen) {
		return null;
	}

	public Boolean iFramesBasedOnWeapon() {
		return iFramesBasedOnWeapon.get();
	}
	public Boolean bowFatigue() {
		return bowFatigue.get();
	}
	public Boolean bedrockBridging() {
		return bedrockBridging.get();
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
	public Boolean creativeAttackReach() {
		return creativeAttackReach.get();
	}
	public Boolean attackDecay() {
		return attackDecay.get().enabled;
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
	public Boolean sweepConditionsMatchMiss() {
		return sweepConditionsMatchMiss.get();
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
	public Boolean delayedEntityUpdates() {
		return delayedEntityUpdates.get();
	}
	public Boolean strengthAppliesToEnchants() {
		return strengthAppliesToEnchants.get();
	}
	public Boolean percentageDamageEffects() {
		return percentageDamageEffects.get();
	}
	public KnockbackMode knockbackMode() {
		return knockbackMode.get();
	}
	public Boolean tierDamageNerf() {
		return tierDamageNerf.get();
	}
	public Boolean tridentVoidReturn() {
		return tridentVoidReturn.get();
	}
	public Boolean disableLoyaltyOnHitEntity() {
		return disableLoyaltyOnHitEntity.get();
	}
	public Boolean dispensableTridents() {
		return dispensableTridents.get();
	}
	public Boolean fishingHookKB() {
		return fishingHookKB.get();
	}
	public Boolean shieldOnlyWhenCharged() {
		return shieldOnlyWhenCharged.get();
	}
	public Boolean letVanillaConnect() {
		return letVanillaConnect.get();
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
	public Boolean improvedMiscEntityAttacks() {
		return improvedMiscEntityAttacks.get();
	}
	public Boolean hasteFix() {
		return hasteFix.get();
	}
	public Boolean mobsCanGuard() {
		return mobsCanGuard.get();
	}
	public Boolean mobsCanSprint() {
		return mobsCanSprint.get();
	}
	public Boolean mobsUsePlayerAttributes() {
		return mobsUsePlayerAttributes.get();
	}
	public Boolean enableDebugLogging() {
		return enableDebugLogging.get();
	}
	public Integer shieldDelay() {
		return shieldDelay.get();
	}
	public Integer instantHealthBonus() {
		return instantHealthBonus.get();
	}
	public double attackDecayMinCharge() {
		return attackDecay.get().minCharge.doubleValue() / 100;
	}
	public double attackDecayMaxCharge() {
		return attackDecay.get().maxCharge.doubleValue() / 100;
	}
	public double attackDecayMaxChargeDiff() {
		double ret = attackDecayMaxCharge() - attackDecayMinCharge();
		if (ret <= 0)
			ret = 1;
		return ret;
	}
	public double attackDecayMinPercentageBase() {
		return attackDecay.get().minPercentageBase.doubleValue() / 100;
	}
	public double attackDecayMaxPercentageBaseDiff() {
		return (attackDecay.get().maxPercentageBase.doubleValue() / 100) - attackDecayMinPercentageBase();
	}
	public double attackDecayMinPercentageEnchants() {
		return attackDecay.get().minPercentageEnchants.doubleValue() / 100;
	}
	public double attackDecayMaxPercentageEnchantsDiff() {
		return (attackDecay.get().maxPercentageEnchants.doubleValue() / 100) - attackDecayMinPercentageEnchants();
	}
	public Integer shieldChargePercentage() {
		return shieldChargePercentage.get();
	}
	public Double fistDamage() {
		return fistDamage.get();
	}
	public Double fallbackShieldDisableTime() {
		return fallbackShieldDisableTime.get();
	}
	public Double snowballDamage() {
		return projectileDamage.get().snowballDamage;
	}
	public Double eggDamage() {
		return projectileDamage.get().eggDamage;
	}
	public Double windChargeDamage() {
		return projectileDamage.get().windChargeDamage;
	}
	public Double thrownTridentDamage() {
		return projectileDamage.get().thrownTridentDamage;
	}
	public Double bowUncertainty() {
		return projectileUncertainty.get().bowUncertainty;
	}
	public Double crossbowUncertainty() {
		return projectileUncertainty.get().crossbowUncertainty;
	}
	public Double baseHandAttackSpeed() {
		return baseHandAttackSpeed.get();
	}
	public Double minHitboxSize() {
		return minHitboxSize.get();
	}
	public EatingInterruptionMode eatingInterruptionMode() {
		return eatingInterruptionMode.get();
	}
	public JSImpl getCritImpl() {
		return critImpl.get();
	}
	public JSImpl getFoodImpl() {
		return foodImpl.get();
	}
	public ArrowDisableMode arrowDisableMode() {
		return arrowDisableMode.get();
	}
	public ArmourPiercingMode armourPiercingMode() {
		return armourPiercingMode.get();
	}

	public void setBridging(boolean allowBridging) {
		bedrockBridging.setValue(allowBridging);
		bedrockBridging.serverManaged = true;
	}

	public static class ProjectileUncertainty implements ConfigRepresentable<ProjectileUncertainty> {
		public static final ProjectileUncertainty DEFAULT = new ProjectileUncertainty(null, 0.25, 0.25);
		public static final StreamCodec<RegistryFriendlyByteBuf, ProjectileUncertainty> STREAM_CODEC = new StreamCodec<>() {
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ProjectileUncertainty projectileUncertainty) {
                registryFriendlyByteBuf.writeResourceLocation(projectileUncertainty.owner.heldValue.owner().name);
                registryFriendlyByteBuf.writeUtf(projectileUncertainty.owner.heldValue.name());
                registryFriendlyByteBuf.writeDouble(projectileUncertainty.bowUncertainty);
				registryFriendlyByteBuf.writeDouble(projectileUncertainty.crossbowUncertainty);
            }

            @NotNull
			@SuppressWarnings("unchecked")
            public ProjectileUncertainty decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                AtlasConfig config = AtlasConfig.configs.get(registryFriendlyByteBuf.readResourceLocation());
                return new ProjectileUncertainty((ConfigHolder<ProjectileUncertainty>) config.valueNameToConfigHolderMap.get(registryFriendlyByteBuf.readUtf()), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readDouble());
            }
        };
		public ConfigHolder<ProjectileUncertainty> owner;
		public Double bowUncertainty;
		public Double crossbowUncertainty;
		public Double bowUncertainty() {
			return bowUncertainty;
		}
		public Double crossbowUncertainty() {
			return crossbowUncertainty;
		}
		public static final Map<String, Field> fields = Util.make(new HashMap<>(), (hashMap) -> {
			try {
				hashMap.put("bowUncertainty", ProjectileUncertainty.class.getDeclaredField("bowUncertainty"));
				hashMap.put("crossbowUncertainty", ProjectileUncertainty.class.getDeclaredField("crossbowUncertainty"));
			} catch (NoSuchFieldException ignored) {
			}

		});
		public static final BiFunction<ProjectileUncertainty, String, Component> convertFieldToComponent = (projectileUncertainty, string) -> {
			try {
				return Component.translatable(projectileUncertainty.owner.getTranslationKey() + "." + string).append(Component.literal(": ")).append(Component.literal(String.valueOf(projectileUncertainty.fieldRepresentingHolder(string).get(projectileUncertainty))));
			} catch (IllegalAccessException var3) {
				return Component.translatable(projectileUncertainty.owner.getTranslationKey() + "." + string);
			}
		};
		public static final BiFunction<ProjectileUncertainty, String, Component> convertFieldToNameComponent = (projectileUncertainty, string) -> Component.translatable(projectileUncertainty.owner.getTranslationKey() + "." + string);
		public static final BiFunction<ProjectileUncertainty, String, Component> convertFieldToValueComponent = (projectileUncertainty, string) -> {
			try {
				return Component.literal(String.valueOf(projectileUncertainty.fieldRepresentingHolder(string).get(projectileUncertainty)));
			} catch (IllegalAccessException var3) {
				return Component.translatable(projectileUncertainty.owner.getTranslationKey() + "." + string);
			}
		};
		public Supplier<Component> resetTranslation = null;

		public ProjectileUncertainty(ConfigHolder<ProjectileUncertainty> owner, Double bowUncertainty, Double crossbowUncertainty) {
			this.owner = owner;
			this.bowUncertainty = Mth.clamp(bowUncertainty, 0, 4);
			this.crossbowUncertainty = Mth.clamp(crossbowUncertainty, 0, 4);
		}

		@Override
		public Codec<ProjectileUncertainty> getCodec(ConfigHolder<ProjectileUncertainty> configHolder) {
			return RecordCodecBuilder.create(instance ->
				instance.group(Codecs.doubleRange(0, 4).optionalFieldOf("bowUncertainty", 0.25).forGetter(ProjectileUncertainty::bowUncertainty),
					Codecs.doubleRange(0, 4).optionalFieldOf("crossbowUncertainty", 0.25).forGetter(ProjectileUncertainty::crossbowUncertainty))
					.apply(instance, (bowUncertainty, crossbowUncertainty) -> new ProjectileUncertainty(configHolder, bowUncertainty, crossbowUncertainty)));
		}

		@Override
		public void setOwnerHolder(ConfigHolder<ProjectileUncertainty> owner) {
			this.owner = owner;
		}

		@Override
		public List<String> fields() {
			return fields.keySet().stream().toList();
		}

		@Override
		public Component getFieldValue(String name) {
			return convertFieldToValueComponent.apply(this, name);
		}

		@Override
		public Component getFieldName(String name) {
			return convertFieldToNameComponent.apply(this, name);
		}

		@Override
		public void listField(String name, Consumer<Component> input) {
			input.accept(convertFieldToComponent.apply(this, name));
		}

		@Override
		public void listFields(Consumer<Component> input) {
			fields.keySet().forEach((string) -> input.accept(convertFieldToComponent.apply(this, string)));
		}

		@Override
		public Field fieldRepresentingHolder(String name) {
			return fields.get(name);
		}

		@Override
		public ArgumentType<?> argumentTypeRepresentingHolder(String name) {
            Object o;
            try {
                o = fields.get(name).get(this);
            } catch (IllegalAccessException e) {
                return null;
            }
            return switch (o) {
				case Double ignored -> DoubleArgumentType.doubleArg(0.0, 4.0);
				case null, default -> null;
			};
		}

		@Override
		@Environment(EnvType.CLIENT)
		@SuppressWarnings("all")
		public List<AbstractConfigListEntry<?>> transformIntoConfigEntries() {
			if (this.resetTranslation == null) this.resetTranslation = () -> Component.translatable(this.owner.getTranslationResetKey());
			List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
			entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "bowUncertainty"), this.bowUncertainty, this.resetTranslation.get(), () -> 0.25, (uncertainty) -> this.bowUncertainty = Mth.clamp(uncertainty, 0.0, 4.0), Optional::empty, false));
			entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "crossbowUncertainty"), this.crossbowUncertainty, this.resetTranslation.get(), () -> 0.25, (uncertainty) -> this.crossbowUncertainty = Mth.clamp(uncertainty, 0.0, 4.0), Optional::empty, false));
			entries.forEach((entry) -> entry.setEditable(!this.owner.serverManaged));
			return entries;
		}
	}
}
