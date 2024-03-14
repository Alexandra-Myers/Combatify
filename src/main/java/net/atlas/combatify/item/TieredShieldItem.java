package net.atlas.combatify.item;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
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
	public static final Item WOODEN_SHIELD = registerItem(id("wooden_shield"), new TieredShieldItem(Tiers.WOOD, new Item.Properties().durability(Tiers.WOOD.getUses() * 6)));
	public static final Item IRON_SHIELD = registerItem(id("iron_shield"), new TieredShieldItem(Tiers.IRON, new Item.Properties().durability(Tiers.IRON.getUses() * 3)));
	public static final Item GOLD_SHIELD = registerItem(id("golden_shield"), new TieredShieldItem(Tiers.GOLD, new Item.Properties().durability(Tiers.GOLD.getUses() * 6)));
	public static final Item DIAMOND_SHIELD = registerItem(id("diamond_shield"), new TieredShieldItem(Tiers.DIAMOND, new Item.Properties().durability(Tiers.DIAMOND.getUses() * 2)));
	public static final Item NETHERITE_SHIELD = registerItem(id("netherite_shield"), new TieredShieldItem(Tiers.NETHERITE, new Item.Properties().durability(Tiers.NETHERITE.getUses() * 2).fireResistant()));

	public TieredShieldItem(Tier tier, Properties properties) {
		super(properties.component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY));
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
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(self());
			if (configurableItemData.tier != null)
				return configurableItemData.tier;
		}
		return tier;
	}
	public static void init() {

	}

	@Override
	public BlockingType getBlockingType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.blockingType != null) {
				return configurableItemData.blockingType;
			}
			if (configurableItemData.type != null && Combatify.ITEMS.configuredWeapons.containsKey(configurableItemData.type)) {
				ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(configurableItemData.type);
				if (configurableWeaponData.blockingType != null) {
					return configurableWeaponData.blockingType;
				}
			}
		}
		return Combatify.registeredTypes.get("new_shield");
	}
}
