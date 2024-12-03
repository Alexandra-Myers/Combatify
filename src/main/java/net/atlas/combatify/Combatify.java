package net.atlas.combatify;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.atlas.atlascore.util.ArrayListExtensions;
import net.atlas.atlascore.util.PrefixLogger;
import net.atlas.combatify.attributes.CustomAttributes;
import net.atlas.combatify.config.CombatifyGeneralConfig;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.extensions.ExtendedTier;
import net.atlas.combatify.item.CombatifyItemTags;
import net.atlas.combatify.item.ItemRegistry;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.world.item.Items.NETHERITE_SWORD;

@SuppressWarnings("unused")
public class Combatify implements ModInitializer {
	public static final String MOD_ID = "combatify";
	public static CombatifyGeneralConfig CONFIG = new CombatifyGeneralConfig();
	public static ItemConfig ITEMS;
	public static ResourceLocation modDetectionNetworkChannel = id("networking");
	public NetworkingHandler networkingHandler;
	public static boolean isCTS = false;
	public static boolean isLoaded = false;
	public static final List<TieredShieldItem> shields = new ArrayListExtensions<>();
	public static final List<UUID> unmoddedPlayers = new ArrayListExtensions<>();
	public static final List<UUID> moddedPlayers = new ArrayListExtensions<>();
	public static final Map<Item, ItemAttributeModifiers> originalModifiers = Util.make(new Object2ObjectOpenHashMap<>(), object2ObjectOpenHashMap -> object2ObjectOpenHashMap.defaultReturnValue(ItemAttributeModifiers.EMPTY));
	public static final Map<UUID, Boolean> isPlayerAttacking = new HashMap<>();
	public static final Map<String, WeaponType> defaultWeaponTypes = new HashMap<>();
	public static final Map<String, BlockingType> defaultTypes = new HashMap<>();
	public static final BiMap<String, Tier> defaultTiers = HashBiMap.create();
	public static Map<String, WeaponType> registeredWeaponTypes = new HashMap<>();
	public static Map<String, BlockingType> registeredTypes = new HashMap<>();
	public static BiMap<String, Tier> tiers = HashBiMap.create();
	public static final PrefixLogger LOGGER = new PrefixLogger(LogManager.getLogger("Combatify"));
	public static final ResourceLocation CHARGED_REACH_ID = id("charged_reach");
	public static final BlockingType SWORD = defineDefaultBlockingType(new SwordBlockingType("sword").setToolBlocker(true).setDisablement(false).setCrouchable(false).setBlockHit(true).setRequireFullCharge(false).setSwordBlocking(true).setDelay(false));
	public static final BlockingType SHIELD = defineDefaultBlockingType(new ShieldBlockingType("shield"));
	public static final BlockingType SHIELD_NO_BANNER = defineDefaultBlockingType(new NonBannerShieldBlockingType("shield_no_banner"));
	public static final BlockingType CURRENT_SHIELD = defineDefaultBlockingType(new CurrentShieldBlockingType("current_shield"));
	public static final BlockingType NEW_SHIELD = defineDefaultBlockingType(new NewShieldBlockingType("new_shield").setKbMechanics(false));
	public static final BlockingType EMPTY = new EmptyBlockingType("empty").setDisablement(false).setCrouchable(false).setRequireFullCharge(false).setKbMechanics(false);

	public static void markCTS(boolean isCTS) {
		Combatify.isCTS = isCTS;
	}

	@Override
	public void onInitialize() {
		isLoaded = true;
		WeaponType.init();
		networkingHandler = new NetworkingHandler();
		AttackEntityCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, pos, direction) -> {
			if(Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResult.PASS;
		});
		AttackBlockCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, pos, direction) -> {
			if(Combatify.unmoddedPlayers.contains(player.getUUID())) {
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
				HitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), direction, pos, false);
				hitResult = MethodHandler.redirectResult(player, hitResult);
				if (hitResult.getType() == HitResult.Type.ENTITY && player instanceof ServerPlayer serverPlayer) {
					serverPlayer.connection.handleInteract(ServerboundInteractPacket.createAttackPacket(((EntityHitResult) hitResult).getEntity(), player.isShiftKeyDown()));
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.PASS;
		});
		UseBlockCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, hitResult) -> {
			if(Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResult.PASS;
		});
		UseEntityCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, entity, hitResult) -> {
			if(Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResult.PASS;
		});
		UseItemCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand) -> {
			if(Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResultHolder.pass(player.getItemInHand(hand));
		});
		LOGGER.info("Init started.");
		CombatifyItemTags.init();
		if (CONFIG.dispensableTridents())
 			DispenserBlock.registerProjectileBehavior(Items.TRIDENT);
		if (CONFIG.configOnlyWeapons()) {
			ItemRegistry.registerWeapons();
			Event<ItemGroupEvents.ModifyEntries> event = ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT);
			event.register(entries -> entries.addAfter(NETHERITE_SWORD, ItemRegistry.WOODEN_KNIFE, ItemRegistry.STONE_KNIFE, ItemRegistry.IRON_KNIFE, ItemRegistry.GOLD_KNIFE, ItemRegistry.DIAMOND_KNIFE, ItemRegistry.NETHERITE_KNIFE, ItemRegistry.WOODEN_LONGSWORD, ItemRegistry.STONE_LONGSWORD, ItemRegistry.IRON_LONGSWORD, ItemRegistry.GOLD_LONGSWORD, ItemRegistry.DIAMOND_LONGSWORD, ItemRegistry.NETHERITE_LONGSWORD));
		}
		if (CONFIG.tieredShields()) {
			TieredShieldItem.init();
			Event<ItemGroupEvents.ModifyEntries> event = ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT);
			event.register(entries -> entries.addAfter(Items.SHIELD, TieredShieldItem.WOODEN_SHIELD, TieredShieldItem.IRON_SHIELD, TieredShieldItem.GOLD_SHIELD, TieredShieldItem.DIAMOND_SHIELD, TieredShieldItem.NETHERITE_SHIELD));
		}

		CustomAttributes.registerAttributes();
		ResourceManagerHelper.registerBuiltinResourcePack(id("combatify_extras"), FabricLoader.getInstance().getModContainer("combatify").get(), Component.translatable("pack.combatify.combatify_extras"), CONFIG.configOnlyWeapons() || CONFIG.tieredShields() ? ResourcePackActivationType.ALWAYS_ENABLED : ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("wooden_shield_recipe"), FabricLoader.getInstance().getModContainer("combatify").get(), Component.translatable("pack.combatify.wooden_shield_recipe"), CONFIG.tieredShields() ? ResourcePackActivationType.ALWAYS_ENABLED : ResourcePackActivationType.NORMAL);
		if (Combatify.CONFIG.percentageDamageEffects()) {
			MobEffects.DAMAGE_BOOST.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("effect.strength"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("effect.weakness"), -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		}
	}

	public static void setDurability(DataComponentMap.Builder builder, @NotNull Item item, int value) {
		builder.set(DataComponents.DAMAGE, 0);
		builder.set(DataComponents.MAX_DAMAGE, value);
		builder.set(DataComponents.MAX_STACK_SIZE, 1);
	}
	public static void registerWeaponType(WeaponType weaponType) {
		Combatify.registeredWeaponTypes.put(weaponType.name, weaponType);
    }
	public static <T extends BlockingType> T registerBlockingType(T blockingType) {
		Combatify.registeredTypes.put(blockingType.getName(), blockingType);
		return blockingType;
	}
	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	public static void defineDefaultWeaponType(WeaponType type) {
		defaultWeaponTypes.put(type.name, type);
	}
	public static <T extends BlockingType> T defineDefaultBlockingType(T blockingType) {
		defaultTypes.put(blockingType.getName(), blockingType);
		return registerBlockingType(blockingType);
	}
	public static void defineDefaultTier(String name, ExtendedTier tier) {
		defaultTiers.put(name, tier);
		tiers.put(name, tier);
	}
}
