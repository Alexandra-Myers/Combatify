package net.atlas.combatify.extensions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.config.ItemConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public record ToolMaterialWrapper(ToolMaterial toolMaterial, int weaponLevel, float blockingLevel) implements Tier {
	public static Codec<Tier> TOOL_MATERIAL_CODEC = Codec.STRING.xmap(ItemConfig::getTier, ItemConfig::getTierName);
	public static Codec<ToolMaterialWrapper> BASE_CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.FLOAT.optionalFieldOf("blocking_level").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.combatify$blockingLevel())),
				Codec.INT.optionalFieldOf("weapon_level").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.combatify$weaponLevel())),
				Codec.INT.optionalFieldOf("enchant_level").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.enchantmentValue())),
				Codec.INT.optionalFieldOf("uses").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.durability())),
				Codec.FLOAT.optionalFieldOf("attack_damage_bonus").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.attackDamageBonus())),
				Codec.FLOAT.optionalFieldOf("mining_speed").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.speed())),
				Codec.withAlternative(TagKey.codec(Registries.ITEM), TagKey.hashedCodec(Registries.ITEM)).optionalFieldOf("repair_items").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.repairItems())),
				Codec.withAlternative(TagKey.codec(Registries.BLOCK), TagKey.hashedCodec(Registries.BLOCK)).optionalFieldOf("incorrect_blocks").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.incorrectBlocksForDrops())),
				TOOL_MATERIAL_CODEC.fieldOf("base_tier").forGetter(toolMaterialWrapper -> toolMaterialWrapper))
			.apply(instance, ToolMaterialWrapper::create));

	public static Codec<ToolMaterialWrapper> FULL_CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.FLOAT.fieldOf("blocking_level").forGetter(ToolMaterialWrapper::combatify$blockingLevel),
				Codec.INT.fieldOf("weapon_level").forGetter(ToolMaterialWrapper::combatify$weaponLevel),
				Codec.INT.fieldOf("enchant_level").forGetter(ToolMaterialWrapper::enchantmentValue),
				Codec.INT.fieldOf("uses").forGetter(ToolMaterialWrapper::durability),
				Codec.FLOAT.fieldOf("attack_damage_bonus").forGetter(ToolMaterialWrapper::attackDamageBonus),
				Codec.FLOAT.fieldOf("mining_speed").forGetter(ToolMaterialWrapper::speed),
				Codec.withAlternative(TagKey.codec(Registries.ITEM), TagKey.hashedCodec(Registries.ITEM)).fieldOf("repair_items").forGetter(ToolMaterialWrapper::repairItems),
				Codec.withAlternative(TagKey.codec(Registries.BLOCK), TagKey.hashedCodec(Registries.BLOCK)).fieldOf("incorrect_blocks").forGetter(ToolMaterialWrapper::incorrectBlocksForDrops))
			.apply(instance, ToolMaterialWrapper::create));

	public static Codec<ToolMaterialWrapper> CODEC = Codec.withAlternative(FULL_CODEC, BASE_CODEC);
	public static ToolMaterialWrapper create(Optional<Float> blockingLevel, Optional<Integer> weaponLevel, Optional<Integer> enchantLevel, Optional<Integer> uses, Optional<Float> damage, Optional<Float> speed, Optional<TagKey<Item>> repairItems, Optional<TagKey<Block>> incorrect, Tier baseTier) {
		return create(blockingLevel.orElse(baseTier.combatify$blockingLevel()), weaponLevel.orElse(baseTier.combatify$weaponLevel()), enchantLevel.orElse(baseTier.enchantmentValue()), uses.orElse(baseTier.durability()), damage.orElse(baseTier.attackDamageBonus()), speed.orElse(baseTier.speed()), repairItems.orElse(baseTier.repairItems()), incorrect.orElse(baseTier.incorrectBlocksForDrops()));
	}
	public static ToolMaterialWrapper create(float blockingLevel, int weaponLevel, int enchantLevel, int uses, float damage, float speed, TagKey<Item> repairItems, TagKey<Block> incorrect) {
		return new ToolMaterialWrapper(new ToolMaterial(incorrect, uses, speed, damage, enchantLevel, repairItems), weaponLevel, blockingLevel);
	}

	@Override
	public TagKey<Block> incorrectBlocksForDrops() {
		return toolMaterial.incorrectBlocksForDrops();
	}

	@Override
	public int durability() {
		return toolMaterial.durability();
	}

	@Override
	public float speed() {
		return toolMaterial.speed();
	}

	@Override
	public float attackDamageBonus() {
		return toolMaterial.attackDamageBonus();
	}

	@Override
	public int enchantmentValue() {
		return toolMaterial.enchantmentValue();
	}

	@Override
	public TagKey<Item> repairItems() {
		return toolMaterial.repairItems();
	}

	@Override
	public int combatify$weaponLevel() {
		return weaponLevel();
	}

	@Override
	public float combatify$blockingLevel() {
		return blockingLevel();
	}
}
