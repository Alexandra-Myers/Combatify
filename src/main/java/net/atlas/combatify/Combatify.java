package net.atlas.combatify;

import net.atlas.combatify.config.ConfigSynchronizer;
import net.atlas.combatify.config.ForgeConfig;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.enchantment.DefendingEnchantment;
import net.atlas.combatify.enchantment.EnchantmentRegistry;
import net.atlas.combatify.enchantment.PiercingEnchantment;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.item.ItemRegistry;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.*;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

import static net.atlas.combatify.item.WeaponType.BASE_ATTACK_SPEED_UUID;
import static net.minecraft.world.item.Items.NETHERITE_SWORD;

@Mod(Combatify.MOD_ID)
@Mod.EventBusSubscriber(modid = Combatify.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Combatify {
	public static Player player;
	public static final String MOD_ID = "combatify";
	public static ForgeConfig CONFIG;
	public static ItemConfig ITEMS;
	public static ResourceLocation modDetectionNetworkChannel = id("networking");
	public NetworkingHandler networkingHandler;
	public static final DeferredRegister<MobEffect> VANILLA_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "minecraft");

	public static final RegistryObject<MobEffect> DAMAGE_BOOST = registerEffect("strength", () -> new DummyAttackDamageMobEffect(MobEffectCategory.BENEFICIAL, 9643043, 0.2)
		.addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));

	public static final RegistryObject<MobEffect> WEAKNESS = registerEffect("weakness", () -> new DummyAttackDamageMobEffect(MobEffectCategory.HARMFUL, 4738376, -0.2)
		.addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
	public static final List<TieredShieldItem> shields = new ArrayListExtensions<>();
	public static final List<UUID> unmoddedPlayers = new ArrayListExtensions<>();
	public static final Map<UUID, Boolean> isPlayerAttacking = new HashMap<>();
	public static final Map<UUID, Boolean> finalizingAttack = new HashMap<>();
	public static final Map<UUID, Timer> scheduleHitResult = new HashMap<>();
	public static Map<String, BlockingType> registeredTypes = new HashMap<>();
	private static <T extends MobEffect> RegistryObject<T> registerEffect(String name, Supplier<T> effect) {
		RegistryObject<T> toReturn = VANILLA_EFFECTS.register(name, effect);
		return toReturn;
	}
	public static final PrefixLogger LOGGER = new PrefixLogger(LogManager.getLogger("Combatify"));
	public static final BlockingType SWORD = registerBlockingType(new SwordBlockingType("sword").setToolBlocker(true).setDisablement(false).setCrouchable(false).setBlockHit(true).setRequireFullCharge(false).setPercentage(true).setSwordBlocking(true));
	public static final BlockingType SHIELD = registerBlockingType(new ShieldBlockingType("shield"));
	public static final BlockingType NEW_SHIELD = registerBlockingType(new NewShieldBlockingType("new_shield").setKbMechanics(false).setPercentage(true));
	public static final BlockingType EMPTY = new EmptyBlockingType("empty").setDisablement(false).setCrouchable(false).setRequireFullCharge(false).setKbMechanics(false);

	public Combatify() {
		Combatify.initConfig();
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		if(CONFIG.configOnlyWeapons.get()) {
			ItemRegistry.registerWeapons(bus);
		}
		if(CONFIG.tieredShields.get()) {
			TieredShieldItem.init(bus);
		}
		if(CONFIG.piercer.get()) {
			PiercingEnchantment.registerEnchants();
		}
		if(CONFIG.defender.get()) {
			DefendingEnchantment.registerEnchants();
		}
		if(CONFIG.piercer.get() || CONFIG.defender.get()) {
			EnchantmentRegistry.registerAllEnchants(bus);
		}

		bus.addListener(this::commonSetup);

		MinecraftForge.EVENT_BUS.register(this);
		VANILLA_EFFECTS.register(bus);
	}
	@SubscribeEvent
	public void attributeModifier(ItemAttributeModifierEvent event) {
		EquipmentSlot equipmentSlot = event.getSlotType();
		if(equipmentSlot == EquipmentSlot.MAINHAND) {
			if(event.getModifiers().containsKey(Attributes.ATTACK_SPEED)) {
				event.getModifiers().get(Attributes.ATTACK_SPEED).forEach(attributeModifier -> {
					if(attributeModifier.getId() == Item.BASE_ATTACK_SPEED_UUID){
						event.removeModifier(Attributes.ATTACK_SPEED, attributeModifier);
						event.addModifier(Attributes.ATTACK_SPEED, calculateSpeed(attributeModifier.getAmount()));
					}
				});
			}
		}
	}
	public AttributeModifier calculateSpeed(double amount) {
		if(amount >= 0) {
			amount = Combatify.CONFIG.fastestToolAttackSpeed.get();
		} else if(amount >= -1) {
			amount = Combatify.CONFIG.fastToolAttackSpeed.get();
		} else if(amount == -2) {
			amount = 0.0;
		} else if(amount >= -2.5) {
			amount = Combatify.CONFIG.fastToolAttackSpeed.get();
		} else if(amount > -3) {
			amount = 0.0;
		} else if (amount > -3.5) {
			amount = Combatify.CONFIG.slowToolAttackSpeed.get();
		} else {
			amount = Combatify.CONFIG.slowestToolAttackSpeed.get();
		}
		return new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", amount, AttributeModifier.Operation.ADDITION);
	}
	public static void initConfig() {
		CONFIG = new ForgeConfig();
	}
	public void commonSetup(FMLCommonSetupEvent event) {
		networkingHandler = new NetworkingHandler();
		ConfigSynchronizer.init();
		LOGGER.info("Init started.");
		DispenserBlock.registerBehavior(Items.TRIDENT, new AbstractProjectileDispenseBehavior() {
			@Override
			protected @NotNull Projectile getProjectile(Level world, Position position, ItemStack stack) {
				ThrownTrident trident = new ThrownTrident(EntityType.TRIDENT, world);
				trident.tridentItem = stack.copy();
				trident.setPosRaw(position.x(), position.y(), position.z());
				trident.pickup = AbstractArrow.Pickup.ALLOWED;
				return trident;
			}
		});
		if (CONFIG.configOnlyWeapons.get()) {
			Event<ItemGroupEvents.ModifyEntries> evt = ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT);
			evt.register(entries -> entries.addAfter(NETHERITE_SWORD, ItemRegistry.WOODEN_KNIFE.get(), ItemRegistry.STONE_KNIFE.get(), ItemRegistry.IRON_KNIFE.get(), ItemRegistry.GOLD_KNIFE.get(), ItemRegistry.DIAMOND_KNIFE.get(), ItemRegistry.NETHERITE_KNIFE.get(), ItemRegistry.WOODEN_LONGSWORD.get(), ItemRegistry.STONE_LONGSWORD.get(), ItemRegistry.IRON_LONGSWORD.get(), ItemRegistry.GOLD_LONGSWORD.get(), ItemRegistry.DIAMOND_LONGSWORD.get(), ItemRegistry.NETHERITE_LONGSWORD.get()));
		}
		if (CONFIG.tieredShields.get()) {
			Event<ItemGroupEvents.ModifyEntries> evt = ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT);
			evt.register(entries -> entries.addAfter(Items.SHIELD, TieredShieldItem.WOODEN_SHIELD.get(), TieredShieldItem.IRON_SHIELD.get(), TieredShieldItem.GOLD_SHIELD.get(), TieredShieldItem.DIAMOND_SHIELD.get(), TieredShieldItem.NETHERITE_SHIELD.get()));
		}
		IForgeRegistry<Item> items = ForgeRegistries.ITEMS;

		for(Item item : items) {
			((ItemExtensions) item).modifyAttributeModifiers();
		}
	}
	public static <T extends BlockingType> T registerBlockingType(T blockingType) {
		Combatify.registeredTypes.put(blockingType.getName(), blockingType);
		return blockingType;
	}
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
