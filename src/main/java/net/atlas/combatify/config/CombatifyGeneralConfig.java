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
import net.atlas.combatify.config.impl.crit.CTSCritImpl;
import net.atlas.combatify.config.impl.crit.CritImpl;
import net.atlas.combatify.config.impl.crit.fixer.CritImplFixer;
import net.atlas.combatify.config.impl.food.CTSFoodImpl;
import net.atlas.combatify.config.impl.food.FoodImpl;
import net.atlas.combatify.config.impl.food.fixer.FoodImplFixer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
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
	private BooleanHolder attributeSwappingFix;
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
	private IntegerHolder aimAssistTicks;
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
	private TagHolder<CritImpl> critImpl;
	private TagHolder<FoodImpl> foodImpl;
	private Category melee;
	private Category ranged;
	private Category defense;
	private Category mob;
	private Category client;
	private Category legacy;
	private Category fixes;
	private Category extras;
	private Category debug;

	public CombatifyGeneralConfig() {
		this(id("combatify-general"));
		declareDefaultForMod("combatify");
	}

	public CombatifyGeneralConfig(Identifier id) {
		super(id);
	}

	@Override
	public Component getFormattedName() {
		return Component.translatableWithFallback("text.config." + this.name.getPath() + ".title", "Combatify General").withStyle(Style.EMPTY.withColor(AtlasCore.CONFIG.configNameDisplayColour.get()));
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
		attackDecay.tieToCategory(melee);
		attackDecay.getFixer().addOldCategory("cts_booleans");
		attackReach = createBoolean("attackReach", true);
		attackReach.tieToCategory(melee);
		attackReach.setupTooltip(1);
		attackReach.getFixer().addOldCategory("cts_booleans");
		autoAttackAllowed = createBoolean("autoAttackAllowed", true);
		autoAttackAllowed.tieToCategory(client);
		autoAttackAllowed.setupTooltip(1);
		autoAttackAllowed.getFixer().addOldCategory("cts_booleans");
		bedrockBridging = createBoolean("bedrockBridging", false);
		bedrockBridging.tieToCategory(client);
		bedrockBridging.setupTooltip(1);
		bedrockBridging.getFixer().addOldCategory("cts_booleans");
		bedrockImpaling = createBoolean("bedrockImpaling", true);
		bedrockImpaling.tieToCategory(melee);
		bedrockImpaling.setupTooltip(1);
		bedrockImpaling.getFixer().addOldCategory("cts_booleans");
		bowFatigue = createBoolean("bowFatigue", true);
		bowFatigue.tieToCategory(ranged);
		bowFatigue.setupTooltip(4);
		bowFatigue.getFixer().addOldCategory("cts_booleans");
		canAttackEarly = createBoolean("canAttackEarly", false);
		canAttackEarly.tieToCategory(melee);
		canAttackEarly.setupTooltip(1);
		canAttackEarly.getFixer().addOldCategory("cts_booleans");
		canSweepOnMiss = createBoolean("canSweepOnMiss", true);
		canSweepOnMiss.tieToCategory(melee);
		canSweepOnMiss.setupTooltip(1);
		canSweepOnMiss.getFixer().addOldCategory("cts_booleans");
		chargedAttacks = createBoolean("chargedAttacks", true);
		chargedAttacks.tieToCategory(melee);
		chargedAttacks.setupTooltip(1);
		chargedAttacks.getFixer().addOldCategory("cts_booleans");
		chargedReach = createBoolean("chargedReach", true);
		chargedReach.tieToCategory(melee);
		chargedReach.setupTooltip(1);
		chargedReach.getFixer().addOldCategory("cts_booleans");
		creativeAttackReach = createBoolean("creativeAttackReach", false);
		creativeAttackReach.tieToCategory(melee);
		creativeAttackReach.setupTooltip(1);
		creativeAttackReach.getFixer().addOldCategory("cts_booleans");
		ctsMomentumPassedToProjectiles = createBoolean("ctsMomentumPassedToProjectiles", true);
		ctsMomentumPassedToProjectiles.tieToCategory(ranged);
		ctsMomentumPassedToProjectiles.setupTooltip(1);
		ctsMomentumPassedToProjectiles.getFixer().addOldCategory("cts_booleans");
		dispensableTridents = createBoolean("dispensableTridents", true);
		dispensableTridents.tieToCategory(ranged);
		dispensableTridents.setupTooltip(1);
		dispensableTridents.getFixer().addOldCategory("cts_booleans");
		hasMissTime = createBoolean("hasMissTime", false);
		hasMissTime.tieToCategory(melee);
		hasMissTime.setupTooltip(1);
		hasMissTime.getFixer().addOldCategory("cts_booleans");
		hasteFix = createBoolean("hasteFix", false);
		hasteFix.tieToCategory(fixes);
		hasteFix.setupTooltip(1);
		hasteFix.getFixer().addOldCategory("cts_booleans");
		iFramesBasedOnWeapon = createBoolean("iFramesBasedOnWeapon", true);
		iFramesBasedOnWeapon.tieToCategory(melee);
		iFramesBasedOnWeapon.setupTooltip(1);
		iFramesBasedOnWeapon.getFixer().addOldCategory("cts_booleans");
		missedAttackRecovery = createBoolean("missedAttackRecovery", true);
		missedAttackRecovery.tieToCategory(melee);
		missedAttackRecovery.setupTooltip(1);
		missedAttackRecovery.getFixer().addOldCategory("cts_booleans");
		percentageDamageEffects = createBoolean("percentageDamageEffects", true);
		percentageDamageEffects.tieToCategory(melee);
		percentageDamageEffects.setupTooltip(1);
		percentageDamageEffects.getFixer().addOldCategory("cts_booleans");
		projectilesHaveIFrames = createBoolean("projectilesHaveIFrames", false);
		projectilesHaveIFrames.tieToCategory(ranged);
		projectilesHaveIFrames.setupTooltip(1);
		projectilesHaveIFrames.getFixer().addOldCategory("cts_booleans");
		resetOnItemChange = createBoolean("resetOnItemChange", false);
		resetOnItemChange.tieToCategory(melee);
		resetOnItemChange.setupTooltip(1);
		resetOnItemChange.getFixer().addOldCategory("cts_booleans");
		snowballKB = createBoolean("snowballKB", true);
		snowballKB.tieToCategory(ranged);
		snowballKB.setupTooltip(1);
		snowballKB.getFixer().addOldCategory("cts_booleans");
		strengthAppliesToEnchants = createBoolean("strengthAppliesToEnchants", true);
		strengthAppliesToEnchants.tieToCategory(melee);
		strengthAppliesToEnchants.setupTooltip(1);
		strengthAppliesToEnchants.getFixer().addOldCategory("cts_booleans");
		sweepWithSweeping = createBoolean("sweepWithSweeping", true);
		sweepWithSweeping.tieToCategory(melee);
		sweepWithSweeping.setupTooltip(1);
		sweepWithSweeping.getFixer().addOldCategory("cts_booleans");
		swingThroughGrass = createBoolean("swingThroughGrass", true);
		swingThroughGrass.tieToCategory(melee);
		swingThroughGrass.setupTooltip(1);
		swingThroughGrass.getFixer().addOldCategory("cts_booleans");
		tierDamageNerf = createBoolean("tierDamageNerf", true);
		tierDamageNerf.tieToCategory(melee);
		tierDamageNerf.setupTooltip(1);
		tierDamageNerf.getFixer().addOldCategory("cts_booleans");
		tridentVoidReturn = createBoolean("tridentVoidReturn", true);
		tridentVoidReturn.tieToCategory(ranged);
		tridentVoidReturn.setupTooltip(1);
		tridentVoidReturn.getFixer().addOldCategory("cts_booleans");

		aimAssistTicks = createInRange("aimAssistTicks", 0, 0, 10, true);
		aimAssistTicks.tieToCategory(melee);
		aimAssistTicks.setupTooltip(1);
		aimAssistTicks.getFixer().addOldCategory("cts_integers");
		shieldDelay = createInRange("shieldDelay", 0, 0, 2000, false);
		shieldDelay.tieToCategory(defense);
		shieldDelay.setupTooltip(1);
		shieldDelay.getFixer().addOldCategory("cts_integers");
		instantHealthBonus = createInRange("instantHealthBonus", 6, 1, 1000, false);
		instantHealthBonus.tieToCategory(defense);
		instantHealthBonus.getFixer().addOldCategory("cts_integers");

		baseHandAttackSpeed = createInRange("baseHandAttackSpeed", 2.5, 2.5, 20);
		baseHandAttackSpeed.tieToCategory(melee);
		baseHandAttackSpeed.getFixer().addOldCategory("cts_doubles");
		fistDamage = createInRange("fistDamage", 2.0, 1, 1024);
		fistDamage.tieToCategory(melee);
		fistDamage.setupTooltip(1);
		fistDamage.getFixer().addOldCategory("cts_doubles");
		projectileUncertainty = createObject("projectileUncertainty", ProjectileUncertainty.DEFAULT, ProjectileUncertainty.class, ProjectileUncertainty.STREAM_CODEC, false);
		projectileUncertainty.tieToCategory(ranged);
		projectileUncertainty.getFixer().addOldCategory("cts_doubles");
		fallbackShieldDisableTime = createInRange("fallbackShieldDisableTime", 1.6, 0, 10);
		fallbackShieldDisableTime.tieToCategory(defense);
		fallbackShieldDisableTime.setupTooltip(1);
		fallbackShieldDisableTime.getFixer().addOldCategory("cts_doubles");
		minHitboxSize = createInRange("minHitboxSize", 0.9, 0, 5);
		minHitboxSize.tieToCategory(mob);
		minHitboxSize.setupTooltip(1);
		minHitboxSize.getFixer().addOldCategory("cts_doubles");

		critImpl = createCodecBacked("critImpl", new CTSCritImpl(true, -1, 1.5F), CritImpl.CODEC);
		critImpl.tieToCategory(melee);
		critImpl.setupTooltip(1);
		critImpl.setFixer(new CritImplFixer(critImpl));
		critImpl.getFixer().addOldCategory("cts_enums");
		eatingInterruptionMode = createEnum("eatingInterruptionMode", EatingInterruptionMode.FULL_RESET, EatingInterruptionMode.class, EatingInterruptionMode.values(), e -> Component.translatable("text.config.combatify-general.option.eatingInterruptionMode." + e.name().toLowerCase(Locale.ROOT)));
		eatingInterruptionMode.tieToCategory(melee);
		eatingInterruptionMode.setupTooltip(4);
		eatingInterruptionMode.getFixer().addOldCategory("cts_enums");
		knockbackMode = createEnum("knockbackMode", KnockbackMode.CTS_8C, KnockbackMode.class, KnockbackMode.values(), e -> Component.translatable("text.config.combatify-general.option.knockbackMode." + e.name().toLowerCase(Locale.ROOT)));
		knockbackMode.tieToCategory(melee);
		knockbackMode.setupTooltip(6);
		knockbackMode.getFixer().addOldCategory("cts_enums");
		foodImpl = createCodecBacked("foodImpl", new CTSFoodImpl(true, true, 6, 7, 21, 0.5F, 2.0F, 2.0F), FoodImpl.CODEC);
		foodImpl.tieToCategory(defense);
		foodImpl.setupTooltip(1);
		foodImpl.setFixer(new FoodImplFixer(foodImpl));
		foodImpl.getFixer().addOldCategory("cts_enums");

		armorPiercingDisablesShields = createBoolean("armorPiercingDisablesShields", false);
		armorPiercingDisablesShields.tieToCategory(defense);
		armorPiercingDisablesShields.setupTooltip(1);
		armorPiercingDisablesShields.getFixer().addOldCategory("extra_booleans");
		attackSpeed = createBoolean("attackSpeed", true);
		attackSpeed.tieToCategory(extras);
		attackSpeed.setupTooltip(1);
		attackSpeed.getFixer().addOldCategory("extra_booleans");
		attributeSwappingFix = createBoolean("attributeSwappingFix", false);
		attributeSwappingFix.tieToCategory(fixes);
		attributeSwappingFix.getFixer().addOldCategory("extra_booleans");
		canInteractWhenCrouchShield = createBoolean("canInteractWhenCrouchShield", true);
		canInteractWhenCrouchShield.tieToCategory(defense);
		canInteractWhenCrouchShield.getFixer().addOldCategory("extra_booleans");
		configOnlyWeapons = createBoolean("configOnlyWeapons", false);
		configOnlyWeapons.tieToCategory(extras);
		configOnlyWeapons.setRestartRequired(RestartRequiredMode.RESTART_BOTH);
		configOnlyWeapons.setupTooltip(1);
		configOnlyWeapons.getFixer().addOldCategory("extra_booleans");
		tieredShields = createBoolean("tieredShields", false);
		tieredShields.tieToCategory(extras);
		tieredShields.setRestartRequired(RestartRequiredMode.RESTART_BOTH);
		tieredShields.setupTooltip(1);
		tieredShields.getFixer().addOldCategory("extra_booleans");
		delayedEntityUpdates = createBoolean("delayedEntityUpdates", false);
		delayedEntityUpdates.tieToCategory(fixes);
		delayedEntityUpdates.setupTooltip(1);
		delayedEntityUpdates.getFixer().addOldCategory("extra_booleans");
		disableLoyaltyOnHitEntity = createBoolean("disableLoyaltyOnHitEntity", false);
		disableLoyaltyOnHitEntity.tieToCategory(extras);
		disableLoyaltyOnHitEntity.getFixer().addOldCategory("extra_booleans");
		fishingHookKB = createBoolean("fishingHookKB", false);
		fishingHookKB.tieToCategory(legacy);
		fishingHookKB.setupTooltip(1);
		fishingHookKB.getFixer().addOldCategory("extra_booleans");
		improvedMiscEntityAttacks = createBoolean("improvedMiscEntityAttacks", false);
		improvedMiscEntityAttacks.tieToCategory(extras);
		improvedMiscEntityAttacks.setupTooltip(1);
		improvedMiscEntityAttacks.getFixer().addOldCategory("extra_booleans");
		instaAttack = createBoolean("instaAttack", false);
		instaAttack.tieToCategory(legacy);
		instaAttack.setupTooltip(1);
		instaAttack.getFixer().addOldCategory("extra_booleans");
		letVanillaConnect = createBoolean("letVanillaConnect", true);
		letVanillaConnect.tieToCategory(extras);
		letVanillaConnect.setupTooltip(1);
		letVanillaConnect.getFixer().addOldCategory("extra_booleans");
		magicHasIFrames = createBoolean("magicHasIFrames", true);
		magicHasIFrames.tieToCategory(extras);
		magicHasIFrames.setupTooltip(1);
		magicHasIFrames.getFixer().addOldCategory("extra_booleans");
		mobsCanGuard = createBoolean("mobsCanGuard", false);
		mobsCanGuard.tieToCategory(mob);
		mobsCanGuard.setupTooltip(1);
		mobsCanGuard.getFixer().addOldCategory("extra_booleans");
		mobsCanSprint = createBoolean("mobsCanSprint", false);
		mobsCanSprint.tieToCategory(mob);
		mobsCanSprint.setupTooltip(1);
		mobsCanSprint.getFixer().addOldCategory("extra_booleans");
		mobsUsePlayerAttributes = createBoolean("mobsUsePlayerAttributes", false);
		mobsUsePlayerAttributes.tieToCategory(mob);
		mobsUsePlayerAttributes.setupTooltip(1);
		mobsUsePlayerAttributes.getFixer().addOldCategory("extra_booleans");
		shieldOnlyWhenCharged = createBoolean("shieldOnlyWhenCharged", false);
		shieldOnlyWhenCharged.tieToCategory(legacy);
		shieldOnlyWhenCharged.setupTooltip(2);
		shieldOnlyWhenCharged.getFixer().addOldCategory("extra_booleans");
		sweepConditionsMatchMiss = createBoolean("sweepConditionsMatchMiss", false);
		sweepConditionsMatchMiss.tieToCategory(extras);
		sweepConditionsMatchMiss.setupTooltip(2);
		sweepConditionsMatchMiss.getFixer().addOldCategory("extra_booleans");
		sweepingNegatedForTamed = createBoolean("sweepingNegatedForTamed", false);
		sweepingNegatedForTamed.tieToCategory(extras);
		sweepingNegatedForTamed.setupTooltip(1);
		sweepingNegatedForTamed.getFixer().addOldCategory("extra_booleans");

		shieldChargePercentage = createInRange("shieldChargePercentage", 195, 1, 200, true);
		shieldChargePercentage.tieToCategory(legacy);
		shieldChargePercentage.setupTooltip(1);
		shieldChargePercentage.getFixer().addOldCategory("extra_integers");

		projectileDamage = createObject("projectileDamage", ProjectileDamage.DEFAULT, ProjectileDamage.class, ProjectileDamage.STREAM_CODEC);
		projectileDamage.tieToCategory(ranged);
		projectileDamage.getFixer().addOldCategory("extra_doubles");

		arrowDisableMode = createEnum("arrowDisableMode", ArrowDisableMode.NONE, ArrowDisableMode.class, ArrowDisableMode.values(), e -> Component.translatable("text.config.combatify-general.option.arrowDisableMode." + e.name().toLowerCase(Locale.ROOT)));
		arrowDisableMode.tieToCategory(ranged);
		arrowDisableMode.setupTooltip(7);
		arrowDisableMode.getFixer().addOldCategory("extra_enums");
		armourPiercingMode = createEnum("armourPiercingMode", ArmourPiercingMode.VANILLA, ArmourPiercingMode.class, ArmourPiercingMode.values(), e -> Component.translatable("text.config.combatify-general.option.armourPiercingMode." + e.name().toLowerCase(Locale.ROOT)));
		armourPiercingMode.tieToCategory(defense);
		armourPiercingMode.setupTooltip(4);
		armourPiercingMode.getFixer().addOldCategory("extra_enums");

		enableDebugLogging = createBoolean("enableDebugLogging", false);
		enableDebugLogging.tieToCategory(debug);
	}

	@Override
	public @NotNull List<Category> createCategories() {
		List<Category> categoryList = super.createCategories();
		melee = new Category(this, "melee_options", new ArrayList<>());
		ranged = new Category(this, "ranged_options", new ArrayList<>());
		defense = new Category(this, "defense_options", new ArrayList<>());
		mob = new Category(this, "mob_options", new ArrayList<>());
		client = new Category(this, "client_controls", new ArrayList<>());
		legacy = new Category(this, "legacy_options", new ArrayList<>());
		fixes = new Category(this, "fixes", new ArrayList<>());
		extras = new Category(this, "extra_options", new ArrayList<>());
		debug = new Category(this, "debug_options", new ArrayList<>());
		categoryList.add(melee);
		categoryList.add(ranged);
		categoryList.add(defense);
		categoryList.add(mob);
		categoryList.add(client);
		categoryList.add(legacy);
		categoryList.add(fixes);
		categoryList.add(extras);
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
						MobEffects.STRENGTH.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, Identifier.withDefaultNamespace("effect.strength"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
						MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, Identifier.withDefaultNamespace("effect.weakness"), -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
					} else {
						MobEffects.STRENGTH.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, Identifier.withDefaultNamespace("effect.strength"), 3.0, AttributeModifier.Operation.ADD_VALUE);
						MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, Identifier.withDefaultNamespace("effect.weakness"), -4.0, AttributeModifier.Operation.ADD_VALUE);
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
	public Boolean attributeSwappingFix() {
		return attributeSwappingFix.get();
	}
	public Boolean strengthAppliesToEnchants() {
		return strengthAppliesToEnchants.get();
	}
	public Boolean percentageDamageEffects() {
		return percentageDamageEffects.get();
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
	public Integer aimAssistTicks() {
		return aimAssistTicks.get();
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
	public KnockbackMode knockbackMode() {
		return knockbackMode.get();
	}
	public CritImpl getCritImpl() {
		return critImpl.get();
	}
	public FoodImpl getFoodImpl() {
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
		public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull ProjectileUncertainty> STREAM_CODEC = new StreamCodec<>() {
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ProjectileUncertainty projectileUncertainty) {
                registryFriendlyByteBuf.writeIdentifier(projectileUncertainty.owner.heldValue.owner().name);
                registryFriendlyByteBuf.writeUtf(projectileUncertainty.owner.heldValue.name());
                registryFriendlyByteBuf.writeDouble(projectileUncertainty.bowUncertainty);
				registryFriendlyByteBuf.writeDouble(projectileUncertainty.crossbowUncertainty);
            }

            @NotNull
			@SuppressWarnings("unchecked")
            public ProjectileUncertainty decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                AtlasConfig config = AtlasConfig.configs.get(registryFriendlyByteBuf.readIdentifier());
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
				hashMap.put("bow_uncertainty", ProjectileUncertainty.class.getDeclaredField("bowUncertainty"));
				hashMap.put("crossbow_uncertainty", ProjectileUncertainty.class.getDeclaredField("crossbowUncertainty"));
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
				instance.group(Codecs.doubleRange(0, 4).optionalFieldOf("bow_uncertainty", 0.25).forGetter(ProjectileUncertainty::bowUncertainty),
					Codecs.doubleRange(0, 4).optionalFieldOf("crossbow_uncertainty", 0.25).forGetter(ProjectileUncertainty::crossbowUncertainty))
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
			entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "bow_uncertainty"), this.bowUncertainty, this.resetTranslation.get(), () -> 0.25, (uncertainty) -> this.bowUncertainty = Mth.clamp(uncertainty, 0.0, 4.0), Optional::empty, false));
			entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "crossbow_uncertainty"), this.crossbowUncertainty, this.resetTranslation.get(), () -> 0.25, (uncertainty) -> this.crossbowUncertainty = Mth.clamp(uncertainty, 0.0, 4.0), Optional::empty, false));
			entries.forEach((entry) -> entry.setEditable(!this.owner.serverManaged));
			return entries;
		}
	}
}
