package net.atlas.combatify.item;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.client.CombatifyBlockEntityWithoutLevelRenderer;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.Tierable;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.atlas.combatify.Combatify.id;
import static net.atlas.combatify.Combatify.shields;

public class TieredShieldItem extends ShieldItem implements Tierable, ItemExtensions {
	public final Tier tier;
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Combatify.MOD_ID);
	public static final RegistryObject<Item> IRON_SHIELD = registerItem(id("iron_shield"), () -> new TieredShieldItem(Tiers.IRON, new Item.Properties().durability(Tiers.IRON.getUses() * 3)));
	public static final RegistryObject<Item> GOLD_SHIELD = registerItem(id("golden_shield"), () -> new TieredShieldItem(Tiers.GOLD, new Item.Properties().durability(Tiers.GOLD.getUses() * 6)));
	public static final RegistryObject<Item> DIAMOND_SHIELD = registerItem(id("diamond_shield"), () -> new TieredShieldItem(Tiers.DIAMOND, new Item.Properties().durability(Tiers.DIAMOND.getUses() * 2)));
	public static final RegistryObject<Item> NETHERITE_SHIELD = registerItem(id("netherite_shield"), () -> new TieredShieldItem(Tiers.NETHERITE, new Item.Properties().durability(Tiers.NETHERITE.getUses() * 2).fireResistant()));

	public TieredShieldItem(Tier tier, Properties properties) {
		super(properties);
		this.tier = tier;
		shields.add(this);
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> this::registerModelPredicate);
	}

	private void registerModelPredicate() {
		//noinspection removal
		ItemProperties.register(this, new ResourceLocation("blocking"), (itemStack, clientWorld, livingEntity, i) ->
			livingEntity != null && livingEntity.isBlocking() && MethodHandler.getBlockingItem(livingEntity).stack() == itemStack ? 1.0F : 0.0F);
	}


	public int getEnchantmentValue() {
		return this.tier.getEnchantmentValue();
	}

	public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
		return this.tier.getRepairIngredient().test(itemStack2);
	}

	@Override
	public Tier getTier() {
		return tier;
	}
	public static void init(IEventBus bus) {
		ITEMS.register(bus);
	}
	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		if (Combatify.CONFIG.tieredShields.get()) {
			consumer.accept(new IClientItemExtensions() {

				@Override
				public BlockEntityWithoutLevelRenderer getCustomRenderer() {
					return new CombatifyBlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
				}
			});
		}
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}

	@Override
	public double getChargedAttackBonus() {
		Item item = this;
		double chargedBonus = 1.0;
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.type != null)
				if (Combatify.ITEMS.configuredWeapons.containsKey(configurableItemData.type))
					if (Combatify.ITEMS.configuredWeapons.get(configurableItemData.type).chargedReach != null)
						chargedBonus = Combatify.ITEMS.configuredWeapons.get(configurableItemData.type).chargedReach;
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
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
	public static RegistryObject<Item> registerItem(ResourceLocation resourceLocation, Supplier<Item> item) {
		return ITEMS.register(resourceLocation.getPath(), item);
	}

	@Override
	public double getPiercingLevel() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(this)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(this);
			if (configurableItemData.piercingLevel != null) {
				return configurableItemData.piercingLevel;
			}
			if (configurableItemData.type != null && Combatify.ITEMS.configuredWeapons.containsKey(configurableItemData.type)) {
				ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(configurableItemData.type);
				if (configurableWeaponData.piercingLevel != null) {
					return configurableWeaponData.piercingLevel;
				}
			}
		}
		return 0;
	}
}
