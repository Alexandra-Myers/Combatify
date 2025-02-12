package net.atlas.combatify.component.generators;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.WeaponType;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.atlas.defaulted.component.generators.WeaponLevelBasedValue;
import net.atlas.defaulted.extension.ItemExtensions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;

public record WeaponStatsGenerator(List<WeaponLevelBasedValue> damage, List<WeaponLevelBasedValue> speed, List<WeaponLevelBasedValue> reach, Optional<ResourceLocation> damageIdOverride, Optional<ResourceLocation> speedIdOverride, Optional<ResourceLocation> reachIdOverride, List<ItemAttributeModifiers.Entry> additionalModifiers, boolean tieredDamage, boolean persistPrevious) implements PatchGenerator {
	public static final MapCodec<WeaponStatsGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(WeaponLevelBasedValue.CODEC.optionalFieldOf("attack_damage", Collections.emptyList()).forGetter(WeaponStatsGenerator::damage),
			WeaponLevelBasedValue.CODEC.optionalFieldOf("attack_speed", Collections.emptyList()).forGetter(WeaponStatsGenerator::speed),
			WeaponLevelBasedValue.CODEC.optionalFieldOf("attack_reach", Collections.emptyList()).forGetter(WeaponStatsGenerator::reach),
			ResourceLocation.CODEC.optionalFieldOf("damage_id_override").forGetter(WeaponStatsGenerator::damageIdOverride),
			ResourceLocation.CODEC.optionalFieldOf("speed_id_override").forGetter(WeaponStatsGenerator::speedIdOverride),
			ResourceLocation.CODEC.optionalFieldOf("reach_id_override").forGetter(WeaponStatsGenerator::reachIdOverride),
			ItemAttributeModifiers.Entry.CODEC.listOf().optionalFieldOf("additional_modifiers", Collections.emptyList()).forGetter(WeaponStatsGenerator::additionalModifiers),
			Codec.BOOL.optionalFieldOf("apply_tier_to_damage", true).forGetter(WeaponStatsGenerator::tieredDamage),
			Codec.BOOL.fieldOf("persist_previous").forGetter(WeaponStatsGenerator::persistPrevious)).apply(instance, WeaponStatsGenerator::new));

	@Override
	public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
		ItemAttributeModifiers oldModifiers = patchedDataComponentMap.get(DataComponents.ATTRIBUTE_MODIFIERS);
		ToolMaterialWrapper toolMaterialWrapper = ((ItemExtensions) item).defaulted$getToolMaterial();
		if (toolMaterialWrapper == null) toolMaterialWrapper = Defaulted.DEFAULT_WRAPPER;
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		double damageModifier = 2 - Combatify.CONFIG.fistDamage();
		ResourceLocation damageID = damageIdOverride.orElse(Item.BASE_ATTACK_DAMAGE_ID);
		ResourceLocation speedID = speedIdOverride.orElse(WeaponType.BASE_ATTACK_SPEED_CTS_ID);
		ResourceLocation reachID = reachIdOverride.orElse(WeaponType.BASE_ATTACK_REACH_ID);
		AttributeModifier attackDamage = null;
		boolean hasDamage = false;
		if (!damage.isEmpty()) {
			hasDamage = true;
			attackDamage = new AttributeModifier(damageID, getTierModifier(toolMaterialWrapper, true, damage) + damageModifier, AttributeModifier.Operation.ADD_VALUE);
		}
		AttributeModifier attackSpeed = null;
		boolean hasSpeed = false;
		if (!speed.isEmpty()) {
			hasSpeed = true;
			attackSpeed = new AttributeModifier(speedID, getTierModifier(toolMaterialWrapper, false, speed), AttributeModifier.Operation.ADD_VALUE);
		}
		AttributeModifier attackReach = null;
		boolean hasReach = false;
		if (!reach.isEmpty()) {
			hasReach = true;
			attackReach = new AttributeModifier(reachID, getTierModifier(toolMaterialWrapper, false, reach), AttributeModifier.Operation.ADD_VALUE);
		}
		if (!(hasDamage || hasSpeed)) return;

		for (ItemAttributeModifiers.Entry entry : additionalModifiers)
			if (!((hasDamage && entry.matches(Attributes.ATTACK_DAMAGE, damageID))
				|| (hasSpeed && isSpeed(entry, speedID))
				|| (hasReach && entry.matches(Attributes.ENTITY_INTERACTION_RANGE, reachID)))) builder.add(entry.attribute(), entry.modifier(), entry.slot());
		if (persistPrevious && oldModifiers != null)
			for (ItemAttributeModifiers.Entry entry : oldModifiers.modifiers())
				if (!((hasDamage && entry.matches(Attributes.ATTACK_DAMAGE, damageID))
				|| (hasSpeed && isSpeed(entry, speedID))
				|| (hasReach && entry.matches(Attributes.ENTITY_INTERACTION_RANGE, reachID)))) builder.add(entry.attribute(), entry.modifier(), entry.slot());

		if (hasDamage) builder.add(Attributes.ATTACK_DAMAGE, attackDamage, EquipmentSlotGroup.MAINHAND);
		if (hasSpeed) builder.add(Attributes.ATTACK_SPEED, attackSpeed, EquipmentSlotGroup.MAINHAND);
		if (hasReach) builder.add(Attributes.ENTITY_INTERACTION_RANGE, attackReach, EquipmentSlotGroup.MAINHAND);
		patchedDataComponentMap.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
	}

	private boolean isSpeed(Entry entry, ResourceLocation speedID) {
		boolean baseRet = entry.matches(Attributes.ATTACK_SPEED, speedID);
		if (speedID.equals(WeaponType.BASE_ATTACK_SPEED_CTS_ID)) baseRet |= entry.matches(Attributes.ATTACK_SPEED, Item.BASE_ATTACK_SPEED_ID);
		return baseRet;
	}

	private double getTierModifier(ToolMaterialWrapper tier, boolean forDamage, List<WeaponLevelBasedValue> list) {
        if (forDamage)
            for (WeaponLevelBasedValue value : list) {
                if (value instanceof WeaponLevelBasedValue.Unconditional unconditional) return unconditional.value() + (tieredDamage ? tier.attackDamageBonus() : 0);
                else {
                    float res = value.getResult(tier.weaponLevel(), tieredDamage);
                    if (res != 0) return res;
                }
            }
        else for (WeaponLevelBasedValue value : list) {
            float res = value.getResult(tier.weaponLevel(), true);
            if (res != 0) return res;
        }
        return 0;
    }

    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
}
