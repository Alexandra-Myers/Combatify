package net.atlas.combatify.item;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.custom.Blocker;
import net.atlas.combatify.component.custom.CanSweep;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID;

public record WeaponType(String name, double damageOffset, double speed, double reach, boolean useHoeDamage, boolean tierable) {
	public static final WeaponType EMPTY = createBasicUntierable("empty", 0, 0, 0);
	public static final WeaponType LONGSWORD = createWithHoeDamageFormula("longsword", 0, 0.5, 1);
	public static final WeaponType KNIFE = createBasic("knife", 1, 1, 0);
	public static final ResourceLocation BASE_ATTACK_SPEED_CTS_ID = ResourceLocation.withDefaultNamespace("base_attack_speed_cts");
	public static final ResourceLocation BASE_ATTACK_REACH_ID = ResourceLocation.withDefaultNamespace("base_attack_reach");

	public static WeaponType createUnsynced(String name, double damageOffset, double speed, double reach, boolean useHoeDamage, boolean tierable) {
		WeaponType type = new WeaponType(name, damageOffset, speed, reach, useHoeDamage, tierable);
		return type;
	}

	public static WeaponType createBasic(String name, double damageOffset, double speed, double reach) {
		return createUnsynced(name, damageOffset, speed, reach, false, true);
	}

	public static WeaponType createBasicUntierable(String name, double damageOffset, double speed, double reach) {
		return createUnsynced(name, damageOffset, speed, reach, false, false);
	}

	public static WeaponType createWithHoeDamageFormula(String name, double damageOffset, double speed, double reach) {
		return createUnsynced(name, damageOffset, speed, reach, true, true);
	}

	public void addCombatAttributes(int weaponLevel, Tier tier, ItemAttributeModifiers.Builder attributeModifiers) {
		if (isEmpty())
			return;
		double speed = this.speedFormula();
		double damage = this.getDamage(weaponLevel, tier.getAttackDamageBonus());
		double reach = this.reach();
		attributeModifiers.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		if (!Combatify.CONFIG.instaAttack())
			attributeModifiers.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_CTS_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		if (reach != 0.0F && Combatify.CONFIG.attackReach())
			attributeModifiers.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ATTACK_REACH_ID, reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
	}

	public double getDamage(int weaponLevel, float attackDamageBonus) {
		double modifier = 2 - Combatify.CONFIG.fistDamage();
		double damageBonus = attackDamageBonus + modifier;
		return damageFormula(damageBonus, weaponLevel, modifier);
	}

	@Override
	public double reach() {
		return reach;
	}

	public Blocker blocking() {
		return null;
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}

	public double damageFormula(double damageBonus, int weaponLevel, double modifier) {
		if (!tierable)
			return modifier + damageOffset;
		else if (useHoeDamage) {
			if (weaponLevel != 2 && weaponLevel != 3) {
				if (weaponLevel >= 4)
					return weaponLevel == 4 ? 2 + modifier : damageBonus - 1;

				return modifier;
			}
			return 1 + modifier;
		}
		return damageBonus + damageOffset;
	}

	public double speedFormula() {
		return speed;
	}

	public static void init() {
		Combatify.defineDefaultWeaponType(LONGSWORD);
		Combatify.defineDefaultWeaponType(KNIFE);
	}
}
