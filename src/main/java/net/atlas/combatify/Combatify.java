package net.atlas.combatify;

import com.google.common.collect.ArrayListMultimap;
import commonnetwork.api.Network;
import net.atlas.combatify.config.ConfigSynchronizer;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ForgeConfig;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.enchantment.DefendingEnchantment;
import net.atlas.combatify.enchantment.EnchantmentRegistry;
import net.atlas.combatify.enchantment.PiercingEnchantment;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.ServerPlayerExtensions;
import net.atlas.combatify.item.ItemRegistry;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.networking.ClientboundResponsePacket;
import net.atlas.combatify.networking.S2CConfigPacket;
import net.atlas.combatify.networking.ItemConfigPacket;
import net.atlas.combatify.networking.PacketRegistration;
import net.atlas.combatify.util.*;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.registries.*;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

import static net.atlas.combatify.item.WeaponType.BASE_ATTACK_SPEED_UUID;

@Mod(Combatify.MOD_ID)
@Mod.EventBusSubscriber(modid = Combatify.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Combatify {
	public static final String MOD_ID = "combatify";
	public static ForgeConfig CONFIG;
	public static ItemConfig ITEMS;
	public static ResourceLocation modDetectionNetworkChannel = id("networking");
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
	public void dedicatedServerStartup(InterModEnqueueEvent event) {
		Combatify.LOGGER.info("Config init started.");
		ITEMS = new ItemConfig();

		IForgeRegistry<Item> items = ForgeRegistries.ITEMS;

		for(Item item : items)
			((ItemExtensions) item).modifyAttributeModifiers();
		for(Item item : ITEMS.configuredItems.keySet()) {
			ConfigurableItemData configurableItemData = ITEMS.configuredItems.get(item);
			if (configurableItemData.stackSize != null)
				((ItemExtensions) item).setStackSize(configurableItemData.stackSize);
		}
		Combatify.LOGGER.info("Loaded items config.");
	}
	@SubscribeEvent
	public void playerAttackBlockEvent(PlayerInteractEvent.LeftClickBlock event) {
		if (Combatify.unmoddedPlayers.contains(event.getEntity().getUUID()) && finalizingAttack.get(event.getEntity().getUUID()) && event.getEntity() instanceof ServerPlayer serverPlayer) {
			Map<HitResult, Float[]> hitResultToRotationMap = ((ServerPlayerExtensions)serverPlayer).getHitResultToRotationMap();
			((ServerPlayerExtensions) serverPlayer).getPresentResult();
			for (HitResult hitResultToChoose : ((ServerPlayerExtensions)serverPlayer).getOldHitResults()) {
				if(hitResultToChoose == null)
					continue;
				Float[] rotations = null;
				if (hitResultToRotationMap.containsKey(hitResultToChoose))
					rotations = hitResultToRotationMap.get(hitResultToChoose);
				float xRot = serverPlayer.getXRot() % 360;
				float yRot = serverPlayer.getYHeadRot() % 360;
				if(rotations != null) {
					float xDiff = Math.abs(xRot - rotations[1]);
					float yDiff = Math.abs(yRot - rotations[0]);
					if(xDiff > 20 || yDiff > 20)
						continue;
				}
				if (hitResultToChoose.getType() == HitResult.Type.ENTITY) {
					event.setCancellationResult(InteractionResult.FAIL);
					event.setCanceled(true);
				}
			}
		}
	}
	@SubscribeEvent
	public void playerUseItemEvent(PlayerInteractEvent.RightClickItem event) {
		if(Combatify.unmoddedPlayers.contains(event.getEntity().getUUID()))
			Combatify.isPlayerAttacking.put(event.getEntity().getUUID(), false);
	}
	@SubscribeEvent
	public void playerUseBlockEvent(PlayerInteractEvent.RightClickBlock event) {
		if(Combatify.unmoddedPlayers.contains(event.getEntity().getUUID()))
			Combatify.isPlayerAttacking.put(event.getEntity().getUUID(), false);
	}
	@SubscribeEvent
	public void playerUseNothingEvent(PlayerInteractEvent.RightClickEmpty event) {
		if(Combatify.unmoddedPlayers.contains(event.getEntity().getUUID()))
			Combatify.isPlayerAttacking.put(event.getEntity().getUUID(), false);
	}
	@SubscribeEvent
	public void playerUseEntityEvent(PlayerInteractEvent.EntityInteract event) {
		if(Combatify.unmoddedPlayers.contains(event.getEntity().getUUID()))
			Combatify.isPlayerAttacking.put(event.getEntity().getUUID(), false);
	}
	@SubscribeEvent
	public void playerUseEntitySpecificEvent(PlayerInteractEvent.EntityInteractSpecific event) {
		if(Combatify.unmoddedPlayers.contains(event.getEntity().getUUID()))
			Combatify.isPlayerAttacking.put(event.getEntity().getUUID(), false);
	}
	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			Network.getNetworkHandler().sendToClient(new ClientboundResponsePacket(), serverPlayer);
			boolean bl = CONFIG.configOnlyWeapons.get() || CONFIG.defender.get() || CONFIG.piercer.get() || !CONFIG.letVanillaConnect.get();
			boolean isModLoaded = NetworkRegistry.getClientNonVanillaNetworkMods().contains("Combatify");
			if (!isModLoaded) {
				if (bl) {
					serverPlayer.connection.disconnect(Component.literal("Combatify needs to be installed on the client to join this server!"));
					return;
				}
				Combatify.unmoddedPlayers.add(serverPlayer.getUUID());
				Combatify.isPlayerAttacking.put(serverPlayer.getUUID(), true);
				Combatify.finalizingAttack.put(serverPlayer.getUUID(), true);
				scheduleHitResult.put(serverPlayer.getUUID(), new Timer());
				Combatify.LOGGER.info("Unmodded player joined: " + serverPlayer.getUUID());
				return;
			}
			if (unmoddedPlayers.contains(serverPlayer.getUUID())) {
				unmoddedPlayers.remove(serverPlayer.getUUID());
				isPlayerAttacking.remove(serverPlayer.getUUID());
				finalizingAttack.remove(serverPlayer.getUUID());
			}
			Combatify.LOGGER.info("Sending server config values to client");
			Network.getNetworkHandler().sendToClient(new S2CConfigPacket(), serverPlayer);
			Network.getNetworkHandler().sendToClient(new ItemConfigPacket(ITEMS), serverPlayer);
			Combatify.LOGGER.info("Config packet sent to client.");
		}
	}
	@SubscribeEvent
	public void disconnect(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			if (unmoddedPlayers.contains(serverPlayer.getUUID())) {
				Timer timer = scheduleHitResult.get(serverPlayer.getUUID());
				timer.cancel();
				timer.purge();
			}
		}
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
		Item item = event.getItemStack().getItem();
		if (ITEMS.configuredItems.containsKey(item) && equipmentSlot == EquipmentSlot.MAINHAND) {
			ConfigurableItemData configurableItemData = ITEMS.configuredItems.get(item);
			if (configurableItemData.type != null) {
				if (event.getModifiers().containsKey(Attributes.ATTACK_DAMAGE)) {
					List<Integer> indexes = new ArrayList<>();
					List<AttributeModifier> modifiers = event.getModifiers().get(Attributes.ATTACK_DAMAGE).stream().toList();
					for (AttributeModifier modifier : modifiers)
						if (modifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID || modifier.getId() == WeaponType.BASE_ATTACK_DAMAGE_UUID)
							indexes.add(modifiers.indexOf(modifier));
					if (!indexes.isEmpty())
						for (Integer index : indexes)
							event.getModifiers().remove(Attributes.ATTACK_DAMAGE, modifiers.get(index));
				}
				if (event.getModifiers().containsKey(Attributes.ATTACK_SPEED)) {
					List<Integer> indexes = new ArrayList<>();
					List<AttributeModifier> modifiers = event.getModifiers().get(Attributes.ATTACK_SPEED).stream().toList();
					for (AttributeModifier modifier : modifiers)
						if (modifier.getId() == Item.BASE_ATTACK_SPEED_UUID || modifier.getId() == WeaponType.BASE_ATTACK_SPEED_UUID)
							indexes.add(modifiers.indexOf(modifier));
					if (!indexes.isEmpty())
						for (Integer index : indexes)
							event.getModifiers().remove(Attributes.ATTACK_SPEED, modifiers.get(index));
				}
				if (event.getModifiers().containsKey(ForgeMod.ENTITY_REACH.get())) {
					List<Integer> indexes = new ArrayList<>();
					List<AttributeModifier> modifiers = event.getModifiers().get(ForgeMod.ENTITY_REACH.get()).stream().toList();
					for (AttributeModifier modifier : modifiers)
						if (modifier.getId() == WeaponType.BASE_ATTACK_REACH_UUID)
							indexes.add(modifiers.indexOf(modifier));
					if (!indexes.isEmpty())
						for (Integer index : indexes)
							event.getModifiers().remove(ForgeMod.ENTITY_REACH.get(), modifiers.get(index));
				}
				ArrayListMultimap<Attribute, AttributeModifier> modMap = ArrayListMultimap.create();
				configurableItemData.type.addCombatAttributes(item instanceof TieredItem tieredItem ? tieredItem.getTier() : Tiers.NETHERITE, modMap);
				event.getModifiers().putAll(modMap);
			}
			if (configurableItemData.damage != null) {
				if (event.getModifiers().containsKey(Attributes.ATTACK_DAMAGE)) {
					List<Integer> indexes = new ArrayList<>();
					List<AttributeModifier> modifiers = event.getModifiers().get(Attributes.ATTACK_DAMAGE).stream().toList();
					for (AttributeModifier modifier : modifiers)
						if (modifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID || modifier.getId() == WeaponType.BASE_ATTACK_DAMAGE_UUID)
							indexes.add(modifiers.indexOf(modifier));
					if (!indexes.isEmpty())
						for (Integer index : indexes)
							event.getModifiers().remove(Attributes.ATTACK_DAMAGE, modifiers.get(index));
				}
				event.getModifiers().put(Attributes.ATTACK_DAMAGE, new AttributeModifier(WeaponType.BASE_ATTACK_DAMAGE_UUID, "Config modifier", configurableItemData.damage - (CONFIG.fistDamage.get() ? 1 : 2), AttributeModifier.Operation.ADDITION));
			}
			if (configurableItemData.speed != null) {
				if (event.getModifiers().containsKey(Attributes.ATTACK_SPEED)) {
					List<Integer> indexes = new ArrayList<>();
					List<AttributeModifier> modifiers = event.getModifiers().get(Attributes.ATTACK_SPEED).stream().toList();
					for (AttributeModifier modifier : modifiers)
						if (modifier.getId() == Item.BASE_ATTACK_SPEED_UUID || modifier.getId() == WeaponType.BASE_ATTACK_SPEED_UUID)
							indexes.add(modifiers.indexOf(modifier));
					if (!indexes.isEmpty())
						for (Integer index : indexes)
							event.getModifiers().remove(Attributes.ATTACK_SPEED, modifiers.get(index));
				}
				event.getModifiers().put(Attributes.ATTACK_SPEED, new AttributeModifier(WeaponType.BASE_ATTACK_SPEED_UUID, "Config modifier", configurableItemData.speed - CONFIG.baseHandAttackSpeed.get(), AttributeModifier.Operation.ADDITION));
			}
			if (configurableItemData.reach != null) {
				if (event.getModifiers().containsKey(ForgeMod.ENTITY_REACH.get())) {
					List<Integer> indexes = new ArrayList<>();
					List<AttributeModifier> modifiers = event.getModifiers().get(ForgeMod.ENTITY_REACH.get()).stream().toList();
					for (AttributeModifier modifier : modifiers)
						if (modifier.getId() == WeaponType.BASE_ATTACK_REACH_UUID)
							indexes.add(modifiers.indexOf(modifier));
					if (!indexes.isEmpty())
						for (Integer index : indexes)
							event.getModifiers().remove(ForgeMod.ENTITY_REACH.get(), modifiers.get(index));
				}
				event.getModifiers().put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(WeaponType.BASE_ATTACK_REACH_UUID, "Config modifier", configurableItemData.reach - 2.5, AttributeModifier.Operation.ADDITION));
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
		IForgeRegistry<Item> items = ForgeRegistries.ITEMS;

		for(Item item : items) {
			((ItemExtensions) item).modifyAttributeModifiers();
		}
		new PacketRegistration().init();
	}
	public static <T extends BlockingType> T registerBlockingType(T blockingType) {
		Combatify.registeredTypes.put(blockingType.getName(), blockingType);
		return blockingType;
	}
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
