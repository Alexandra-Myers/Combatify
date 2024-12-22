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

public record ToolMaterialWrapper(ToolMaterial toolMaterial, int level) implements Tier {
	public static Codec<Tier> TOOL_MATERIAL_CODEC = Codec.STRING.xmap(ItemConfig::getTier, ItemConfig::getTierName);
	public static Codec<ToolMaterialWrapper> BASE_CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.INT.optionalFieldOf("mining_level").forGetter(extendedTier -> Optional.of(extendedTier.level())),
				Codec.INT.optionalFieldOf("enchant_level").forGetter(extendedTier -> Optional.of(extendedTier.enchantmentValue())),
				Codec.INT.optionalFieldOf("uses").forGetter(extendedTier -> Optional.of(extendedTier.durability())),
				Codec.FLOAT.optionalFieldOf("attack_damage_bonus").forGetter(extendedTier -> Optional.of(extendedTier.attackDamageBonus())),
				Codec.FLOAT.optionalFieldOf("mining_speed").forGetter(extendedTier -> Optional.of(extendedTier.speed())),
				Codec.withAlternative(TagKey.codec(Registries.ITEM), TagKey.hashedCodec(Registries.ITEM)).optionalFieldOf("repair_items").forGetter(extendedTier -> Optional.of(extendedTier.repairItems())),
				Codec.withAlternative(TagKey.codec(Registries.BLOCK), TagKey.hashedCodec(Registries.BLOCK)).optionalFieldOf("incorrect_blocks").forGetter(extendedTier -> Optional.of(extendedTier.incorrectBlocksForDrops())),
				TOOL_MATERIAL_CODEC.fieldOf("base_tier").forGetter(toolMaterialWrapper -> toolMaterialWrapper))
			.apply(instance, ToolMaterialWrapper::create));

	public static Codec<ToolMaterialWrapper> FULL_CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.INT.fieldOf("mining_level").forGetter(ToolMaterialWrapper::level),
				Codec.INT.fieldOf("enchant_level").forGetter(ToolMaterialWrapper::enchantmentValue),
				Codec.INT.fieldOf("uses").forGetter(ToolMaterialWrapper::durability),
				Codec.FLOAT.fieldOf("attack_damage_bonus").forGetter(ToolMaterialWrapper::attackDamageBonus),
				Codec.FLOAT.fieldOf("mining_speed").forGetter(ToolMaterialWrapper::speed),
				Codec.withAlternative(TagKey.codec(Registries.ITEM), TagKey.hashedCodec(Registries.ITEM)).fieldOf("repair_items").forGetter(ToolMaterialWrapper::repairItems),
				Codec.withAlternative(TagKey.codec(Registries.BLOCK), TagKey.hashedCodec(Registries.BLOCK)).fieldOf("incorrect_blocks").forGetter(ToolMaterialWrapper::incorrectBlocksForDrops))
			.apply(instance, ToolMaterialWrapper::create));

	public static Codec<ToolMaterialWrapper> CODEC = Codec.withAlternative(FULL_CODEC, BASE_CODEC);
	public static int getLevel(Tier tier) {
		return tier.level();
	}
	public static ToolMaterialWrapper create(Optional<Integer> level, Optional<Integer> enchantLevel, Optional<Integer> uses, Optional<Float> damage, Optional<Float> speed, Optional<TagKey<Item>> repairItems, Optional<TagKey<Block>> incorrect, Tier baseTier) {
		return create(level.orElse(getLevel(baseTier)), enchantLevel.orElse(baseTier.enchantmentValue()), uses.orElse(baseTier.durability()), damage.orElse(baseTier.attackDamageBonus()), speed.orElse(baseTier.speed()), repairItems.orElse(baseTier.repairItems()), incorrect.orElse(baseTier.incorrectBlocksForDrops()));
	}
	public static ToolMaterialWrapper create(int level, int enchantLevel, int uses, float damage, float speed, TagKey<Item> repairItems, TagKey<Block> incorrect) {
		return new ToolMaterialWrapper(new ToolMaterial(incorrect, uses, speed, damage, enchantLevel, repairItems), level);
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
}
