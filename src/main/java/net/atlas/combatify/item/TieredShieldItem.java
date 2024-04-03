package net.atlas.combatify.item;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import static net.atlas.combatify.Combatify.id;
import static net.atlas.combatify.Combatify.shields;
import static net.atlas.combatify.item.ItemRegistry.registerItem;

public class TieredShieldItem extends ShieldItem implements ItemExtensions {
	public final Tier tier;
	public static final Item WOODEN_SHIELD = registerItem(id("wooden_shield"), new TieredShieldItem(Tiers.WOOD, new Item.Properties()));
	public static final Item IRON_SHIELD = registerItem(id("iron_shield"), new TieredShieldItem(Tiers.IRON, new Item.Properties()));
	public static final Item GOLD_SHIELD = registerItem(id("golden_shield"), new TieredShieldItem(Tiers.GOLD, new Item.Properties()));
	public static final Item DIAMOND_SHIELD = registerItem(id("diamond_shield"), new TieredShieldItem(Tiers.DIAMOND, new Item.Properties()));
	public static final Item NETHERITE_SHIELD = registerItem(id("netherite_shield"), new TieredShieldItem(Tiers.NETHERITE, new Item.Properties().fireResistant()));

	public TieredShieldItem(Tier tier, Properties properties) {
		super(properties.durability(tier.getUses() * 2).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY));
		this.tier = tier;
		shields.add(this);
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
			registerModelPredicate();
	}

	private void registerModelPredicate() {
		ItemProperties.register(this, new ResourceLocation("blocking"), (itemStack, clientWorld, livingEntity, i) ->
			livingEntity != null && livingEntity.isUsingItem() && MethodHandler.getBlockingItem(livingEntity) == itemStack ? 1.0F : 0.0F);
	}


	public int getEnchantmentValue() {
		return getConfigTier().getEnchantmentValue();
	}

	public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
		return getConfigTier().getRepairIngredient().test(itemStack2) || super.isValidRepairItem(itemStack, itemStack2);
	}

	@Override
	public Item self() {
		return this;
	}

	@Override
	public Tier getConfigTier() {
		Tier tier = getTierFromConfig();
		if (tier != null) return tier;
		return this.tier;
	}
	public static void init() {

	}

	@Override
	public BlockingType getBlockingType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.blockingType != null)
				return configurableItemData.blockingType;
			WeaponType type;
			if ((type = configurableItemData.type) != null && Combatify.ITEMS.configuredWeapons.containsKey(type)) {
				BlockingType blockingType = Combatify.ITEMS.configuredWeapons.get(type).blockingType;
				if (blockingType != null)
					return blockingType;
			}
		}
		return Combatify.registeredTypes.get("new_shield");
	}
}
