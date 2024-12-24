package net.atlas.combatify.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.custom.Blocker;
import net.atlas.combatify.component.custom.CanSweep;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.extensions.Tier;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID;

public record WeaponType(String name, double damageOffset, double speed, double reach, boolean useAxeDamage,
						 boolean useHoeDamage, boolean useHoeSpeed, boolean tierable) {
	public static final StreamCodec<? super FriendlyByteBuf, WeaponType> STREAM_CODEC = StreamCodec.of((buf, weaponType) -> buf.writeUtf(weaponType.name), buf -> WeaponType.fromID(buf.readUtf()));
	public static final WeaponType EMPTY = createBasicUntierable("empty", 0, 0, 0);
	public static final WeaponType SWORD = createBasic("sword", 2, 0.5, 0.5);
	public static final WeaponType MACE = createBasic("mace", 2, -1.5, 0);
	public static final WeaponType LONGSWORD = createWithHoeDamageFormula("longsword", 0, 0.5, 1);
	public static final WeaponType AXE = createAxe("axe", 3, -0.5, 0);
	public static final WeaponType PICKAXE = createBasic("pickaxe", 1, 0, 0);
	public static final WeaponType HOE = createWithHoeDamageFormulaAndSpeed("hoe", 0, 1, 1);
	public static final WeaponType SHOVEL = createBasic("shovel", 0, -0.5, 0);
	public static final WeaponType KNIFE = createBasic("knife", 1, 1, 0.25);
	public static final WeaponType TRIDENT = createWithAxeDamageFormula("trident", 3, -0.5, 1);
	public static final Codec<WeaponType> SIMPLE_CODEC = Codec.STRING.xmap(weapon_type -> fromID(weapon_type.toLowerCase(Locale.ROOT)), WeaponType::name);
	public static final Codec<WeaponType> STRICT_CODEC = SIMPLE_CODEC.validate(weapon_type -> weapon_type == null ? DataResult.error(() -> "Attempted to retrieve a Weapon Type that does not exist!") : DataResult.success(weapon_type));
	public static final MapCodec<WeaponType> FULL_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(Codec.STRING.fieldOf("name").forGetter(WeaponType::name),
				Codec.DOUBLE.fieldOf("damage_offset").forGetter(WeaponType::damageOffset),
				Codec.DOUBLE.fieldOf("speed").forGetter(WeaponType::speed),
				Codec.DOUBLE.fieldOf("reach").forGetter(WeaponType::reach),
				Codec.BOOL.optionalFieldOf("tierable", true).forGetter(WeaponType::tierable))
			.apply(instance, (name, damage, speed, reach, tierable) -> tierable ? createBasic(name, damage, speed, reach) : createBasicUntierable(name, damage, speed, reach)));
	public static final MapCodec<List<WeaponType>> CODEC_DECODE = Codec.mapEither(Codec.withAlternative(STRICT_CODEC.listOf(), STRICT_CODEC, Collections::singletonList).fieldOf("name"), FULL_CODEC.xmap(Collections::singletonList, List::getFirst)).xmap(
		Either::unwrap,
		Either::left
	);
	public static final MapCodec<List<WeaponType>> CODEC_ENCODE = Codec.mapEither(FULL_CODEC.xmap(Collections::singletonList, List::getFirst), Codec.withAlternative(STRICT_CODEC.listOf(), STRICT_CODEC, Collections::singletonList).fieldOf("name")).xmap(
		Either::unwrap,
		Either::left
	);
	public static final ResourceLocation BASE_ATTACK_SPEED_CTS_ID = ResourceLocation.withDefaultNamespace("base_attack_speed_cts");
	public static final ResourceLocation BASE_ATTACK_REACH_ID = ResourceLocation.withDefaultNamespace("base_attack_reach");

	public static WeaponType createUnsynced(String name, double damageOffset, double speed, double reach, boolean useAxeDamage, boolean useHoeDamage, boolean useHoeSpeed, boolean tierable) {
		WeaponType type = new WeaponType(name, damageOffset, speed, reach, useAxeDamage, useHoeDamage, useHoeSpeed, tierable);
		if (!Objects.equals(name, "empty"))
			Combatify.registerWeaponType(type);
		return type;
	}

	public static WeaponType createBasic(String name, double damageOffset, double speed, double reach) {
		return createUnsynced(name, damageOffset, speed, reach, false, false, false, true);
	}

	public static WeaponType createBasicUntierable(String name, double damageOffset, double speed, double reach) {
		return createUnsynced(name, damageOffset, speed, reach, false, false, false, false);
	}

	public static WeaponType createWithAxeDamageFormula(String name, double damageOffset, double speed, double reach) {
		return createUnsynced(name, damageOffset, speed, reach, true, false, false, true);
	}

	public static WeaponType createAxe(String name, double damageOffset, double speed, double reach) {
		return createUnsynced(name, damageOffset, speed, reach, true, false, false, true);
	}

	public static WeaponType createWithHoeDamageFormula(String name, double damageOffset, double speed, double reach) {
		return createUnsynced(name, damageOffset, speed, reach, false, true, false, true);
	}

	public static WeaponType createWithHoeDamageFormulaAndSpeed(String name, double damageOffset, double speed, double reach) {
		return createUnsynced(name, damageOffset, speed, reach, false, true, true, true);
	}

	public void addCombatAttributes(Tier tier, ItemAttributeModifiers.Builder attributeModifiers) {
		if (isEmpty())
			return;
		double speed = this.getSpeed(tier);
		double damage = this.getDamage(tier);
		double reach = this.reach();
		attributeModifiers.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		if (!Combatify.CONFIG.instaAttack())
			attributeModifiers.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_CTS_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		if (reach != 0.0F && Combatify.CONFIG.attackReach())
			attributeModifiers.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ATTACK_REACH_ID, reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
	}

	public double getDamage(Tier tier) {
		int modifier = Combatify.CONFIG.fistDamage() ? 1 : 0;
		double damageBonus = tier.attackDamageBonus() + modifier;
		boolean isNotTier1 = tier.combatify$weaponLevel() > 0;
		ConfigurableWeaponData configurableWeaponData = MethodHandler.forWeapon(this);
		if (configurableWeaponData != null) {
			if (configurableWeaponData.attackDamage() != null) {
				if (configurableWeaponData.tiered())
					return damageBonus + configurableWeaponData.attackDamage();
				else
					return modifier + configurableWeaponData.attackDamage();
			}
		}
		return damageFormula(damageBonus, tier, isNotTier1, modifier);
	}

	public double getSpeed(Tier tier) {
		ConfigurableWeaponData configurableWeaponData = MethodHandler.forWeapon(this);
		if (configurableWeaponData != null) {
			if (configurableWeaponData.attackSpeed() != null)
				return configurableWeaponData.attackSpeed() - Combatify.CONFIG.baseHandAttackSpeed();
		}
		return speedFormula(tier);
	}

	@Override
	public double reach() {
		ConfigurableWeaponData configurableWeaponData = MethodHandler.forWeapon(this);
		if (configurableWeaponData != null) {
			if (configurableWeaponData.attackReach() != null)
				return configurableWeaponData.attackReach() - 2.5;
		}
		return reach;
	}

	public double getChargedReach() {
		ConfigurableWeaponData configurableWeaponData = MethodHandler.forWeapon(this);
		if (configurableWeaponData != null) {
			if (configurableWeaponData.chargedReach() != null)
				return configurableWeaponData.chargedReach();
		}
		return 1.0;
	}

	public CanSweep canSweep() {
		ConfigurableWeaponData configurableWeaponData = MethodHandler.forWeapon(this);
		if (configurableWeaponData != null) return configurableWeaponData.canSweep();
		return null;
	}

	public Blocker blocking() {
		ConfigurableWeaponData configurableWeaponData = MethodHandler.forWeapon(this);
		if (configurableWeaponData != null) return configurableWeaponData.blocking();
		return null;
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}

	public static WeaponType fromID(String id) {
		return Combatify.registeredWeaponTypes.get(id);
	}

	public double damageFormula(double damageBonus, Tier tier, boolean isNotTier1, double modifier) {
		if (!tierable)
			return modifier + damageOffset;
		else if (useAxeDamage) {
			if (!Combatify.CONFIG.ctsAttackBalancing())
				return (isNotTier1 ? tier.combatify$weaponLevel() >= 4 ? 8 : 7 : 5) + modifier + (tier.combatify$weaponLevel() >= 4 && !ItemConfig.getTier("netherite").equals(tier) ? damageBonus - 3 : 0);
			else
				return damageBonus + 3.0;
		} else if (useHoeDamage) {
			if (tier.combatify$weaponLevel() != 2 && tier.combatify$weaponLevel() != 3) {
				if (tier.combatify$weaponLevel() >= 4)
					return ItemConfig.getTier("netherite").equals(tier) ? 2 + modifier : damageBonus - 1;

				return modifier;
			}
			return 1 + modifier;
		}
		return damageBonus + damageOffset;
	}

	public double speedFormula(Tier tier) {
		if (useHoeSpeed) {
			if (ItemConfig.getTier("wood").equals(tier))
				return -0.5;
			else if (tier.combatify$weaponLevel() == 2 || ItemConfig.getTier("iron").equals(tier))
				return 0.5;
			else if (tier.combatify$weaponLevel() >= 3 || ItemConfig.getTier("gold").equals(tier))
				return 1.0;
			else
				return 0.0;
		}
		return speed;
	}

	public static void init() {
		Combatify.defineDefaultWeaponType(SWORD);
		Combatify.defineDefaultWeaponType(MACE);
		Combatify.defineDefaultWeaponType(LONGSWORD);
		Combatify.defineDefaultWeaponType(AXE);
		Combatify.defineDefaultWeaponType(PICKAXE);
		Combatify.defineDefaultWeaponType(HOE);
		Combatify.defineDefaultWeaponType(SHOVEL);
		Combatify.defineDefaultWeaponType(KNIFE);
		Combatify.defineDefaultWeaponType(TRIDENT);
	}
}
