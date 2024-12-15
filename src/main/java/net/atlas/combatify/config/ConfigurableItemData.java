package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.config.item.ArmourStats;
import net.atlas.combatify.config.item.Blocker;
import net.atlas.combatify.config.item.WeaponStats;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.Objects;
import java.util.Optional;

import static net.atlas.combatify.config.ItemConfig.getTier;
import static net.atlas.combatify.config.ItemConfig.getTierName;

public record ConfigurableItemData(WeaponStats weaponStats, Optional<Integer> optionalStackSize, Optional<Integer> optionalCooldown, Boolean cooldownAfter, Blocker blocker,
								   Optional<Integer> optionalEnchantability, Optional<Boolean> optionalIsEnchantable, Optional<Integer> optionalUseDuration,
								   Optional<Tier> optionalTier, ArmourStats armourStats, Optional<Ingredient> optionalIngredient, Optional<TagKey<Block>> optionalTag, Optional<Tool> optionalTool,
								   ItemAttributeModifiers itemAttributeModifiers) {

	public static final ConfigurableItemData EMPTY = new ConfigurableItemData(WeaponStats.EMPTY, (Integer) null, null, null, Blocker.EMPTY, null, null, null, null, ArmourStats.EMPTY, null, null, null, ItemAttributeModifiers.EMPTY);
	public static final Codec<ConfigurableItemData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(WeaponStats.CODEC.optionalFieldOf("weapon_information", WeaponStats.EMPTY).forGetter(ConfigurableItemData::weaponStats),
				Codec.INT.optionalFieldOf("stack_size").forGetter(ConfigurableItemData::optionalStackSize),
				Codec.INT.optionalFieldOf("cooldown").forGetter(ConfigurableItemData::optionalCooldown),
				Codec.BOOL.optionalFieldOf("cooldown_after", true).forGetter(ConfigurableItemData::cooldownAfter),
				Blocker.CODEC.optionalFieldOf("blocking_information", Blocker.EMPTY).forGetter(ConfigurableItemData::blocker),
				Codec.INT.optionalFieldOf("enchantment_level").forGetter(ConfigurableItemData::optionalEnchantability),
				Codec.BOOL.optionalFieldOf("is_enchantable").forGetter(ConfigurableItemData::optionalIsEnchantable),
				Codec.INT.optionalFieldOf("use_duration").forGetter(ConfigurableItemData::optionalUseDuration),
				Codec.STRING.optionalFieldOf("tier").xmap(name -> name.map(ItemConfig::getTier), tier1 -> tier1.map(ItemConfig::getTierName)).forGetter(ConfigurableItemData::optionalTier),
				ArmourStats.CODEC.optionalFieldOf("armor_information", ArmourStats.EMPTY).forGetter(ConfigurableItemData::armourStats),
				Ingredient.CODEC.optionalFieldOf("repair_ingredient").forGetter(ConfigurableItemData::optionalIngredient),
				Codec.withAlternative(TagKey.codec(Registries.BLOCK), TagKey.hashedCodec(Registries.BLOCK)).optionalFieldOf("tool_tag").forGetter(ConfigurableItemData::optionalTag),
				Tool.CODEC.optionalFieldOf("tool").forGetter(ConfigurableItemData::optionalTool),
				ItemAttributeModifiers.CODEC.optionalFieldOf("item_attribute_modifiers", ItemAttributeModifiers.EMPTY).forGetter(ConfigurableItemData::itemAttributeModifiers))
			.apply(instance, ConfigurableItemData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurableItemData> ITEM_DATA_STREAM_CODEC = StreamCodec.of((buf, configurableItemData) -> {
		WeaponStats.STREAM_CODEC.encode(buf, configurableItemData.weaponStats);
		Blocker.STREAM_CODEC.encode(buf, configurableItemData.blocker);
		ArmourStats.STREAM_CODEC.encode(buf, configurableItemData.armourStats);
		buf.writeVarInt(configurableItemData.optionalStackSize.orElse(-10));
		buf.writeVarInt(configurableItemData.optionalCooldown.orElse(-10));
		if(configurableItemData.optionalCooldown.isPresent())
			buf.writeBoolean(configurableItemData.cooldownAfter);
		buf.writeVarInt(configurableItemData.optionalEnchantability.orElse(-10));
		buf.writeInt(configurableItemData.optionalIsEnchantable.map(aBoolean -> aBoolean ? 1 : 0).orElse(-10));
		buf.writeVarInt(configurableItemData.optionalUseDuration.orElse(-10));
		buf.writeUtf(configurableItemData.optionalTier.isEmpty() ? "empty" : getTierName(configurableItemData.optionalTier.get()));
		buf.writeBoolean(configurableItemData.optionalIngredient.isEmpty());
        configurableItemData.optionalIngredient.ifPresent(ingredient -> Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient));
		buf.writeBoolean(configurableItemData.optionalTag.isEmpty());
        configurableItemData.optionalTag.ifPresent(blockTagKey -> buf.writeResourceLocation(blockTagKey.location()));
		buf.writeBoolean(configurableItemData.optionalTool.isEmpty());
        configurableItemData.optionalTool.ifPresent(tool -> Tool.STREAM_CODEC.encode(buf, tool));
		ItemAttributeModifiers.STREAM_CODEC.encode(buf, configurableItemData.itemAttributeModifiers);
	}, buf -> {
		WeaponStats weaponStats = WeaponStats.STREAM_CODEC.decode(buf);
		Blocker blocker = Blocker.STREAM_CODEC.decode(buf);
		ArmourStats armourStats = ArmourStats.STREAM_CODEC.decode(buf);
		Integer stackSize = buf.readVarInt();
		Integer cooldown = buf.readVarInt();
		Boolean cooldownAfter = null;
		if (cooldown != -10)
			cooldownAfter = buf.readBoolean();
		Integer enchantlevel = buf.readVarInt();
		int isEnchantableAsInt = buf.readInt();
		Boolean isEnchantable = null;
		Integer useDuration = buf.readVarInt();
		Tier tier = getTier(buf.readUtf());
		boolean repairIngredientAbsent = buf.readBoolean();
		Ingredient ingredient = repairIngredientAbsent ? null : Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
		boolean toolMineableAbsent = buf.readBoolean();
		TagKey<Block> toolMineable = toolMineableAbsent ? null : TagKey.create(Registries.BLOCK, buf.readResourceLocation());
		boolean toolAbsent = buf.readBoolean();
		Tool tool = toolAbsent ? null : Tool.STREAM_CODEC.decode(buf);
		ItemAttributeModifiers attributeModifiers = ItemAttributeModifiers.STREAM_CODEC.decode(buf);
		if (stackSize == -10)
			stackSize = null;
		if (cooldown == -10)
			cooldown = null;
		if (enchantlevel == -10)
			enchantlevel = null;
		if (isEnchantableAsInt != -10)
			isEnchantable = isEnchantableAsInt == 1;
		if (useDuration == -10)
			useDuration = null;
        return new ConfigurableItemData(weaponStats, stackSize, cooldown, cooldownAfter, blocker, enchantlevel, isEnchantable, useDuration, tier, armourStats, ingredient, toolMineable, tool, attributeModifiers);
	});

	public ConfigurableItemData(WeaponStats weaponStats, Integer optionalStackSize, Integer optionalCooldown, Boolean cooldownAfter,
								Blocker blocker, Integer optionalEnchantability, Boolean optionalIsEnchantable, Integer optionalUseDuration,
								Tier optionalTier, ArmourStats armourStats, Ingredient optionalIngredient, TagKey<Block> optionalTag, Tool optionalTool, ItemAttributeModifiers itemAttributeModifiers) {
		this(weaponStats, Optional.ofNullable(optionalStackSize), Optional.ofNullable(optionalCooldown), cooldownAfter, blocker, Optional.ofNullable(optionalEnchantability), Optional.ofNullable(optionalIsEnchantable), Optional.ofNullable(optionalUseDuration), Optional.ofNullable(optionalTier), armourStats, Optional.ofNullable(optionalIngredient), Optional.ofNullable(optionalTag), Optional.ofNullable(optionalTool), itemAttributeModifiers);
	}
    public ConfigurableItemData(WeaponStats weaponStats, Optional<Integer> optionalStackSize, Optional<Integer> optionalCooldown, Boolean cooldownAfter, Blocker blocker,
								Optional<Integer> optionalEnchantability, Optional<Boolean> optionalIsEnchantable, Optional<Integer> optionalUseDuration,
								Optional<Tier> optionalTier, ArmourStats armourStats, Optional<Ingredient> optionalIngredient, Optional<TagKey<Block>> optionalTag, Optional<Tool> optionalTool,
								ItemAttributeModifiers itemAttributeModifiers) {
		this.weaponStats = weaponStats;
		this.optionalStackSize = clamp(optionalStackSize, 1, 99);
		this.optionalCooldown = clamp(optionalCooldown, 1, 1000);
		this.cooldownAfter = cooldownAfter;
		this.blocker = blocker;
		this.optionalEnchantability = clamp(optionalEnchantability, 0, 1000);
		this.optionalIsEnchantable = optionalIsEnchantable;
		this.optionalUseDuration = clamp(optionalUseDuration, 1, 1000);
        this.optionalTier = optionalTier;
		this.armourStats = armourStats;
        this.optionalIngredient = optionalIngredient;
		this.optionalTag = optionalTag;
        this.optionalTool = optionalTool;
		this.itemAttributeModifiers = itemAttributeModifiers;
    }

	public Integer stackSize() {
		return optionalStackSize.orElse(null);
	}

	public Integer cooldown() {
		return optionalCooldown.orElse(null);
	}

	public Integer enchantability() {
		return optionalEnchantability.orElse(null);
	}

	public Boolean isEnchantable() {
		return optionalIsEnchantable.orElse(null);
	}

	public Integer useDuration() {
		return optionalUseDuration.orElse(null);
	}

	public Tier tier() {
		return optionalTier.orElse(null);
	}

	public Ingredient repairIngredient() {
		return optionalIngredient.orElse(null);
	}

	public TagKey<Block> toolMineableTag() {
		return optionalTag.orElse(null);
	}

	public Tool tool() {
		return optionalTool.orElse(null);
	}

	public static Optional<Integer> max(Optional<Integer> value, int min) {
        return value.map(integer -> Math.max(integer, min));
    }

	public static Optional<Double> max(Optional<Double> value, double min) {
		return value.map(val -> Math.max(val, min));
	}

	public static Optional<Integer> clamp(Optional<Integer> value, int min, int max) {
		return value.map(integer -> Math.min(Math.max(integer, min), max));
	}

	public static Optional<Double> clamp(Optional<Double> value, double min, double max) {
		return value.map(val -> Math.min(Math.max(val, min), max));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConfigurableItemData that)) return false;
        return Objects.equals(weaponStats, that.weaponStats) && Objects.equals(optionalStackSize, that.optionalStackSize) && Objects.equals(optionalCooldown, that.optionalCooldown) && Objects.equals(cooldownAfter, that.cooldownAfter) && Objects.equals(blocker, that.blocker) && Objects.equals(optionalEnchantability, that.optionalEnchantability) && Objects.equals(optionalIsEnchantable, that.optionalIsEnchantable) && Objects.equals(optionalUseDuration, that.optionalUseDuration) && Objects.equals(optionalTier, that.optionalTier) && Objects.equals(armourStats, that.armourStats) && Objects.equals(optionalIngredient, that.optionalIngredient) && Objects.equals(optionalTag, that.optionalTag) && Objects.equals(optionalTool, that.optionalTool) && Objects.equals(itemAttributeModifiers, that.itemAttributeModifiers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(weaponStats, optionalStackSize, optionalCooldown, cooldownAfter, blocker, optionalEnchantability, optionalIsEnchantable, optionalUseDuration, optionalTier, armourStats, optionalIngredient, optionalTag, optionalTool, itemAttributeModifiers);
	}
}
