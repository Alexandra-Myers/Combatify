package net.atlas.combatify;

import com.google.common.base.Suppliers;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.atlas.atlascore.util.ArrayListExtensions;
import net.atlas.atlascore.util.PrefixLogger;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.component.CustomEnchantmentEffectComponents;
import net.atlas.combatify.component.custom.Blocker;
import net.atlas.combatify.component.generators.WeaponStatsGenerator;
import net.atlas.combatify.config.CombatifyGeneralConfig;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.critereon.ItemSubPredicateInit;
import net.atlas.combatify.item.CombatifyItemTags;
import net.atlas.combatify.item.ItemRegistry;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.MethodHandler;
import net.atlas.combatify.util.blocking.BlockingType;
import net.atlas.combatify.util.blocking.BlockingTypeInit;
import net.atlas.combatify.util.blocking.condition.BlockingConditions;
import net.atlas.combatify.util.blocking.effect.PostBlockEffects;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.fabric.component.DefaultedRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Context;

import java.lang.ref.Cleaner;
import java.util.*;
import java.util.function.Supplier;

import static net.minecraft.world.item.Items.NETHERITE_SWORD;

@SuppressWarnings("unused")
public class Combatify implements ModInitializer {
	public static final String MOD_ID = "combatify";
	public static final PrefixLogger LOGGER = new PrefixLogger(LogManager.getLogger("Combatify"));
	public static final PrefixLogger JS_LOGGER = new PrefixLogger(LogManager.getLogger("Combatify|JavaScript"));
	public static final Cleaner CLEANER = Cleaner.create();
	public static CombatifyGeneralConfig CONFIG = new CombatifyGeneralConfig();
	public static ItemConfig ITEMS;
	public static ResourceLocation modDetectionNetworkChannel = id("networking");
	public NetworkingHandler networkingHandler;
	public static Supplier<CombatifyState> state = Suppliers.memoize(() -> CombatifyState.COMBATIFY);
	public static boolean isLoaded = false;
	public static boolean mobConfigIsDirty = true;
	public static final List<Item> shields = new ArrayListExtensions<>();
	public static final List<UUID> unmoddedPlayers = new ArrayListExtensions<>();
	public static final List<UUID> moddedPlayers = new ArrayListExtensions<>();
	public static final Map<Holder<Item>, ItemAttributeModifiers> originalModifiers = Util.make(new Object2ObjectOpenHashMap<>(), object2ObjectOpenHashMap -> object2ObjectOpenHashMap.defaultReturnValue(ItemAttributeModifiers.EMPTY));
	public static final Map<UUID, Boolean> isPlayerAttacking = new HashMap<>();
	public static final Map<String, WeaponType> defaultWeaponTypes = new HashMap<>();
	public static final Map<ResourceLocation, BlockingType> defaultTypes = new HashMap<>();
	public static Map<ResourceLocation, BlockingType> registeredTypes = new HashMap<>();
	public static final ResourceLocation CHARGED_REACH_ID = id("charged_reach");

	public static void markState(Supplier<CombatifyState> state) {
		Combatify.state = state;
	}

	public static CombatifyState getState() {
		return Combatify.state.get();
	}

	@Override
	public void onInitialize() {
		Combatify.CLEANER.register(this, Context::exit);
		isLoaded = true;
		BlockingConditions.bootstrap();
		PostBlockEffects.bootstrap();
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
		CustomDataComponents.registerDataComponents();
		CustomEnchantmentEffectComponents.registerEnchantmentEffectComponents();
		ItemSubPredicateInit.init();
		BlockingTypeInit.init();
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			PolymerItemUtils.ITEM_CHECK.register(itemStack -> isPatched(itemStack.getItem()) || itemStack.has(CustomDataComponents.BLOCKER) || itemStack.has(CustomDataComponents.CAN_SWEEP) || itemStack.has(CustomDataComponents.PIERCING_LEVEL));
		}
		CombatifyItemTags.init();
		if (CONFIG.dispensableTridents())
 			DispenserBlock.registerProjectileBehavior(Items.TRIDENT);
		DefaultItemComponentEvents.MODIFY.register(modDetectionNetworkChannel, (modifyContext) -> {
			modifyContext.modify(Items.WOODEN_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL, 1));
			modifyContext.modify(Items.GOLDEN_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL, 1));
			modifyContext.modify(Items.STONE_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL, 2));
			modifyContext.modify(Items.IRON_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL, 3));
			modifyContext.modify(Items.DIAMOND_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL, 4));
			modifyContext.modify(Items.NETHERITE_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL, 5));
			modifyContext.modify(Items.SHIELD, builder -> builder.set(CustomDataComponents.BLOCKER, Blocker.VANILLA_SHIELD));
		});
		if (CONFIG.configOnlyWeapons()) {
			ItemRegistry.registerWeapons();
			Event<ItemGroupEvents.ModifyEntries> event = ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT);
			event.register(entries -> entries.addAfter(NETHERITE_SWORD, ItemRegistry.WOODEN_KNIFE, ItemRegistry.STONE_KNIFE, ItemRegistry.IRON_KNIFE, ItemRegistry.GOLD_KNIFE, ItemRegistry.DIAMOND_KNIFE, ItemRegistry.NETHERITE_KNIFE, ItemRegistry.WOODEN_LONGSWORD, ItemRegistry.STONE_LONGSWORD, ItemRegistry.IRON_LONGSWORD, ItemRegistry.GOLD_LONGSWORD, ItemRegistry.DIAMOND_LONGSWORD, ItemRegistry.NETHERITE_LONGSWORD));
		}
		if (CONFIG.tieredShields()) {
			TieredShieldItem.init();
			shields.add(Items.SHIELD);
			Event<ItemGroupEvents.ModifyEntries> event = ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT);
			event.register(entries -> entries.addAfter(Items.SHIELD, TieredShieldItem.IRON_SHIELD, TieredShieldItem.GOLD_SHIELD, TieredShieldItem.DIAMOND_SHIELD, TieredShieldItem.NETHERITE_SHIELD));
		}

		DefaultedRegistries.registerPatchGenerator("combat_test_weapon_stats", WeaponStatsGenerator.CODEC);
		ModContainer modContainer = FabricLoader.getInstance().getModContainer("combatify").get();
		ResourceManagerHelper.registerBuiltinResourcePack(id("alternate_mace"), modContainer, Component.translatable("pack.combatify.alternate_mace"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("combatify_extras"), modContainer, Component.translatable("pack.combatify.combatify_extras"), CONFIG.configOnlyWeapons() || CONFIG.tieredShields() ? ResourcePackActivationType.ALWAYS_ENABLED : ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("default_mace"), modContainer, Component.translatable("pack.combatify.default_mace"), ResourcePackActivationType.DEFAULT_ENABLED);
		ResourceManagerHelper.registerBuiltinResourcePack(id("default_shield"), modContainer, Component.translatable("pack.combatify.default_shield"), ResourcePackActivationType.DEFAULT_ENABLED);
		ResourceManagerHelper.registerBuiltinResourcePack(id("default_shield_attacker_kb"), modContainer, Component.translatable("pack.combatify.default_shield_attacker_knockback"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("old_sword_blocking"), modContainer, Component.translatable("pack.combatify.old_sword_blocking"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("percentage_shield"), modContainer, Component.translatable("pack.combatify.percentage_shield"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("shield_no_banner"), modContainer, Component.translatable("pack.combatify.shield_no_banner"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("sword_blocking"), modContainer, Component.translatable("pack.combatify.sword_blocking"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("vanilla_attack_balancing"), modContainer, Component.translatable("pack.combatify.vanilla_attack_balancing"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("weapon_types"), modContainer, Component.translatable("pack.combatify.weapon_types"), ResourcePackActivationType.DEFAULT_ENABLED);
		ResourceManagerHelper.registerBuiltinResourcePack(id("wooden_shield_recipe"), modContainer, Component.translatable("pack.combatify.wooden_shield_recipe"), CONFIG.tieredShields() ? ResourcePackActivationType.DEFAULT_ENABLED : ResourcePackActivationType.NORMAL);
		if (Combatify.CONFIG.percentageDamageEffects()) {
			MobEffects.DAMAGE_BOOST.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("effect.strength"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("effect.weakness"), -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		}
	}

	public static void setDurability(DataComponentPatch.Builder builder, @NotNull Item item, int value) {
		builder.set(DataComponents.DAMAGE, 0);
		builder.set(DataComponents.MAX_DAMAGE, value);
		builder.set(DataComponents.MAX_STACK_SIZE, 1);
	}
	public static BlockingType registerBlockingType(BlockingType blockingType) {
		Combatify.registeredTypes.put(blockingType.name(), blockingType);
		return blockingType;
	}
	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	public static void defineDefaultWeaponType(WeaponType type) {
		defaultWeaponTypes.put(type.name(), type);
	}
	public static BlockingType defineDefaultBlockingType(BlockingType blockingType) {
		defaultTypes.put(blockingType.name(), blockingType);
		return registerBlockingType(blockingType);
	}

	public static boolean isPatched(Item item) {
		List<ItemPatches> patches = DefaultComponentPatchesManager.getCached();
		if (patches == null) return false;
		return patches.stream().anyMatch(itemPatches -> itemPatches.matchItem(item));
	}

	public enum CombatifyState implements StringRepresentable {
		VANILLA("Vanilla", "vanilla"),
		COMBATIFY("Combatify", "combatify"),
		CTS_8C("CTS 8C", "combat_test");

		public final String name;
		public final String key;

		CombatifyState(String name, String key) {
			this.name = name;
			this.key = key;
		}

		@Override
		public @NotNull String getSerializedName() {
			return key;
		}

		public Component getComponent() {
			return Component.translatableWithFallback("options.combatify_state." + key, name);
		}

		@Override
		public String toString() {
			return "CombatifyState{" +
				"name='" + name + '\'' +
				'}';
		}
	}
}
