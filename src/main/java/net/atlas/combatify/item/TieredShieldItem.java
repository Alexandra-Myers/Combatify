package net.atlas.combatify.item;

import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.component.custom.Blocker;
import net.atlas.combatify.util.MethodHandler;
import net.atlas.combatify.util.blocking.BlockingTypeInit;
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

import java.util.Collections;

public class TieredShieldItem extends ShieldItem {
	public final Tier tier;
	public static final Blocker.ItemDamageFunction SHIELD_DAMAGE_FUNCTION = new Blocker.ItemDamageFunction(3.0F, 0.0F, 1.0F);
	public static final Blocker.ItemDamageFunction GOLDEN_SHIELD_DAMAGE_FUNCTION = new Blocker.ItemDamageFunction(4.0F, 0.0F, 0.5F);
	public static final Item IRON_SHIELD = registerItem(id("iron_shield"), properties -> new TieredShieldItem(Tiers.IRON, properties, 0, 3, 3, Blocker.NEW_SHIELD, SHIELD_DAMAGE_FUNCTION), new Item.Properties());
	public static final Item GOLD_SHIELD = registerItem(id("golden_shield"), properties -> new TieredShieldItem(Tiers.GOLD, properties, -0.2F, 1, 4, Blocker.NEW_SHIELD, GOLDEN_SHIELD_DAMAGE_FUNCTION), new Item.Properties());
	public static final Item DIAMOND_SHIELD = registerItem(id("diamond_shield"), properties -> new TieredShieldItem(Tiers.DIAMOND, properties, 0, 5, 2, Blocker.NEW_SHIELD, SHIELD_DAMAGE_FUNCTION),new Item.Properties());
	public static final Item NETHERITE_SHIELD = registerItem(id("netherite_shield"), properties -> new TieredShieldItem(Tiers.NETHERITE, properties, 0, 5, 2, Blocker.NEW_SHIELD.withKnockback(Collections.singletonList(BlockingTypeInit.SHIELD_KNOCKBACK)).withProtection(Collections.singletonList(BlockingTypeInit.NETHERITE_SHIELD_PROTECTION)), SHIELD_DAMAGE_FUNCTION), new Item.Properties().fireResistant());

	public TieredShieldItem(Tier tier, Properties properties, float disableCooldownModifier, int lvl, int tierDurabilityModifier, Blocker baseBlocker, Blocker.ItemDamageFunction itemDamageFunction) {
		super(properties.durability(tier.getUses() * tierDurabilityModifier)
			.component(CustomDataComponents.BLOCKING_LEVEL, lvl)
			.component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
			.component(CustomDataComponents.BLOCKER, baseBlocker.withDisableCooldownScale(1 + disableCooldownModifier).withItemDamageFunction(itemDamageFunction)));
		this.tier = tier;
		shields.add(this);
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
			registerModelPredicate();
	}

	private void registerModelPredicate() {
		ItemProperties.register(this, ResourceLocation.withDefaultNamespace("blocking"), (itemStack, clientWorld, livingEntity, i) ->
			livingEntity != null && livingEntity.isBlocking() && MethodHandler.getBlockingItem(livingEntity).stack() == itemStack ? 1.0F : 0.0F);
	}

	@Override
	public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
		return tier.getRepairIngredient().test(itemStack2);
	}

	@Override
	public int getEnchantmentValue() {
		return tier.getEnchantmentValue();
	}

	public static void init() {

	}
}
