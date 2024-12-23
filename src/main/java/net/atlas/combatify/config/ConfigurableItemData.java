package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.config.item.ArmourStats;
import net.atlas.combatify.config.item.Blocker;
import net.atlas.combatify.config.item.WeaponStats;
import net.atlas.combatify.extensions.Tier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.level.block.Block;

import java.util.Objects;
import java.util.Optional;

import static net.atlas.combatify.config.ItemConfig.getTier;
import static net.atlas.combatify.config.ItemConfig.getTierName;

public record ConfigurableItemData(WeaponStats weaponStats, Optional<Integer> optionalStackSize, Optional<UseCooldown> optionalCooldown, Blocker blocker,
								   Optional<Enchantable> optionalEnchantable, Optional<Integer> optionalUseDuration,
								   Optional<Tier> optionalTier, ArmourStats armourStats, Optional<TagKey<Item>> optionalRepairItems, Optional<TagKey<Block>> optionalTag, Optional<Tool> optionalTool,
								   ItemAttributeModifiers itemAttributeModifiers) {

	public static final ConfigurableItemData EMPTY = new ConfigurableItemData(WeaponStats.EMPTY, (Integer) null, null, Blocker.EMPTY, null, null, null, ArmourStats.EMPTY, null, null, null, ItemAttributeModifiers.EMPTY);
	public static final Codec<ConfigurableItemData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(WeaponStats.CODEC.optionalFieldOf("weapon_information", WeaponStats.EMPTY).forGetter(ConfigurableItemData::weaponStats),
				Codec.INT.optionalFieldOf("stack_size").forGetter(ConfigurableItemData::optionalStackSize),
				UseCooldown.CODEC.optionalFieldOf("cooldown").forGetter(ConfigurableItemData::optionalCooldown),
				Blocker.CODEC.optionalFieldOf("blocking_information", Blocker.EMPTY).forGetter(ConfigurableItemData::blocker),
				Enchantable.CODEC.optionalFieldOf("enchantable").forGetter(ConfigurableItemData::optionalEnchantable),
				Codec.INT.optionalFieldOf("use_duration").forGetter(ConfigurableItemData::optionalUseDuration),
				Codec.STRING.optionalFieldOf("tier").xmap(name -> name.map(ItemConfig::getTier), tier1 -> tier1.map(ItemConfig::getTierName)).forGetter(ConfigurableItemData::optionalTier),
				ArmourStats.CODEC.optionalFieldOf("armor_information", ArmourStats.EMPTY).forGetter(ConfigurableItemData::armourStats),
				Codec.withAlternative(TagKey.codec(Registries.ITEM), TagKey.hashedCodec(Registries.ITEM)).optionalFieldOf("repair_items").forGetter(ConfigurableItemData::optionalRepairItems),
				Codec.withAlternative(TagKey.codec(Registries.BLOCK), TagKey.hashedCodec(Registries.BLOCK)).optionalFieldOf("tool_tag").forGetter(ConfigurableItemData::optionalTag),
				Tool.CODEC.optionalFieldOf("tool").forGetter(ConfigurableItemData::optionalTool),
				ItemAttributeModifiers.CODEC.optionalFieldOf("item_attribute_modifiers", ItemAttributeModifiers.EMPTY).forGetter(ConfigurableItemData::itemAttributeModifiers))
			.apply(instance, ConfigurableItemData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurableItemData> ITEM_DATA_STREAM_CODEC = StreamCodec.of((buf, configurableItemData) -> {
		WeaponStats.STREAM_CODEC.encode(buf, configurableItemData.weaponStats);
		Blocker.STREAM_CODEC.encode(buf, configurableItemData.blocker);
		ArmourStats.STREAM_CODEC.encode(buf, configurableItemData.armourStats);
		buf.writeVarInt(configurableItemData.optionalStackSize.orElse(-10));
		buf.writeBoolean(configurableItemData.optionalCooldown.isPresent());
		configurableItemData.optionalCooldown.ifPresent(useCooldown -> UseCooldown.STREAM_CODEC.encode(buf, useCooldown));
		buf.writeBoolean(configurableItemData.optionalEnchantable.isPresent());
        configurableItemData.optionalEnchantable.ifPresent(enchantable -> Enchantable.STREAM_CODEC.encode(buf, enchantable));
		buf.writeVarInt(configurableItemData.optionalUseDuration.orElse(-10));
		buf.writeUtf(configurableItemData.optionalTier.isEmpty() ? "empty" : getTierName(configurableItemData.optionalTier.get()));
		buf.writeBoolean(configurableItemData.optionalRepairItems.isEmpty());
        configurableItemData.optionalRepairItems.ifPresent(itemTagKey -> buf.writeResourceLocation(itemTagKey.location()));
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
		UseCooldown cooldown = null;
		if (buf.readBoolean())
			cooldown = UseCooldown.STREAM_CODEC.decode(buf);
		Enchantable enchantable = null;
		if (buf.readBoolean())
			enchantable = Enchantable.STREAM_CODEC.decode(buf);
		Integer useDuration = buf.readVarInt();
		Tier tier = getTier(buf.readUtf());
		boolean repairItemsAbsent = buf.readBoolean();
		TagKey<Item> repairItems = repairItemsAbsent ? null : TagKey.create(Registries.ITEM, buf.readResourceLocation());
		boolean toolMineableAbsent = buf.readBoolean();
		TagKey<Block> toolMineable = toolMineableAbsent ? null : TagKey.create(Registries.BLOCK, buf.readResourceLocation());
		boolean toolAbsent = buf.readBoolean();
		Tool tool = toolAbsent ? null : Tool.STREAM_CODEC.decode(buf);
		ItemAttributeModifiers attributeModifiers = ItemAttributeModifiers.STREAM_CODEC.decode(buf);
		if (stackSize == -10)
			stackSize = null;
		if (useDuration == -10)
			useDuration = null;
        return new ConfigurableItemData(weaponStats, stackSize, cooldown, blocker, enchantable, useDuration, tier, armourStats, repairItems, toolMineable, tool, attributeModifiers);
	});

	public ConfigurableItemData(WeaponStats weaponStats, Integer optionalStackSize, UseCooldown optionalCooldown,
								Blocker blocker, Enchantable optionalEnchantable, Integer optionalUseDuration,
								Tier optionalTier, ArmourStats armourStats, TagKey<Item> optionalRepairItems, TagKey<Block> optionalTag, Tool optionalTool, ItemAttributeModifiers itemAttributeModifiers) {
		this(weaponStats, Optional.ofNullable(optionalStackSize), Optional.ofNullable(optionalCooldown), blocker, Optional.ofNullable(optionalEnchantable), Optional.ofNullable(optionalUseDuration), Optional.ofNullable(optionalTier), armourStats, Optional.ofNullable(optionalRepairItems), Optional.ofNullable(optionalTag), Optional.ofNullable(optionalTool), itemAttributeModifiers);
	}
    public ConfigurableItemData(WeaponStats weaponStats, Optional<Integer> optionalStackSize, Optional<UseCooldown> optionalCooldown, Blocker blocker,
								Optional<Enchantable> optionalEnchantable, Optional<Integer> optionalUseDuration,
								Optional<Tier> optionalTier, ArmourStats armourStats, Optional<TagKey<Item>> optionalRepairItems, Optional<TagKey<Block>> optionalTag, Optional<Tool> optionalTool,
								ItemAttributeModifiers itemAttributeModifiers) {
		this.weaponStats = weaponStats;
		this.optionalStackSize = clamp(optionalStackSize, 1, 99);
		this.optionalCooldown = optionalCooldown;
		this.blocker = blocker;
		this.optionalEnchantable = optionalEnchantable;
		this.optionalUseDuration = clamp(optionalUseDuration, 1, 1000);
        this.optionalTier = optionalTier;
		this.armourStats = armourStats;
        this.optionalRepairItems = optionalRepairItems;
		this.optionalTag = optionalTag;
        this.optionalTool = optionalTool;
		this.itemAttributeModifiers = itemAttributeModifiers;
    }

	public Integer stackSize() {
		return optionalStackSize.orElse(null);
	}

	public UseCooldown cooldown() {
		return optionalCooldown.orElse(null);
	}

	public Enchantable enchantable() {
		return optionalEnchantable.orElse(null);
	}

	public Integer useDuration() {
		return optionalUseDuration.orElse(null);
	}

	public Tier tier() {
		return optionalTier.orElse(null);
	}

	public TagKey<Item> repairItems() {
		return optionalRepairItems.orElse(null);
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
        return Objects.equals(weaponStats, that.weaponStats) && Objects.equals(optionalStackSize, that.optionalStackSize) && Objects.equals(optionalCooldown, that.optionalCooldown) && Objects.equals(blocker, that.blocker) && Objects.equals(optionalEnchantable, that.optionalEnchantable) && Objects.equals(optionalUseDuration, that.optionalUseDuration) && Objects.equals(optionalTier, that.optionalTier) && Objects.equals(armourStats, that.armourStats) && Objects.equals(optionalRepairItems, that.optionalRepairItems) && Objects.equals(optionalTag, that.optionalTag) && Objects.equals(optionalTool, that.optionalTool) && Objects.equals(itemAttributeModifiers, that.itemAttributeModifiers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(weaponStats, optionalStackSize, optionalCooldown, blocker, optionalEnchantable, optionalUseDuration, optionalTier, armourStats, optionalRepairItems, optionalTag, optionalTool, itemAttributeModifiers);
	}
}
