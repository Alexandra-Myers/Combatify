package net.atlas.combatify;

import com.google.common.base.Suppliers;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.atlas.atlascore.util.ArrayListExtensions;
import net.atlas.atlascore.util.PrefixLogger;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.component.CustomEnchantmentEffectComponents;
import net.atlas.combatify.component.custom.ExtendedBlockingData;
import net.atlas.combatify.component.generators.CombatifyPatchGenerators;
import net.atlas.combatify.config.CombatifyGeneralConfig;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.config.impl.crit.CritImpl;
import net.atlas.combatify.config.impl.food.FoodImpl;
import net.atlas.combatify.criterion.DataComponentPredicateInit;
import net.atlas.combatify.init.TieredShieldBootstrap;
import net.atlas.combatify.item.CombatifyItemTags;
import net.atlas.combatify.init.ItemRegistry;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.CombatifyState;
import net.atlas.combatify.util.IDUtils;
import net.atlas.combatify.util.MethodHandler;
import net.atlas.combatify.util.blocking.BlockingType;
import net.atlas.combatify.util.blocking.BlockingTypeInit;
import net.atlas.combatify.util.blocking.condition.BlockingConditions;
import net.atlas.combatify.util.blocking.effect.PostBlockEffects;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.component.ItemPatches;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
//? >=26.1 {
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
//?} <26.1 {
/*import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
*///?}
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
//? >=26.1 {
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
//?} <26.1 {
/*import net.minecraft.network.protocol.game.ServerboundInteractPacket;
*///?}
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
//? >=1.21.11 {
import net.minecraft.util.Util;
//?} <1.21.11 {
/*import net.minecraft.util.Util;
import net.minecraft.world.InteractionResultHolder;
*///?}


import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.jspecify.annotations.NonNull;

import java.lang.ref.Cleaner;
import java.util.*;
import java.util.function.Supplier;

import static net.minecraft.world.item.Items.NETHERITE_SWORD;

@SuppressWarnings("unused")
public class Combatify {
	public static final String MOD_ID = "combatify";
	public static final PrefixLogger LOGGER = new PrefixLogger(LogManager.getLogger("Combatify"));
	public static final PrefixLogger JS_LOGGER = new PrefixLogger(LogManager.getLogger("Combatify|JavaScript"));
	public static final Cleaner CLEANER = Cleaner.create();
	public static CombatifyGeneralConfig CONFIG;
	public static ItemConfig ITEMS;
	public static NetworkingHandler networkingHandler;
	private static Supplier<CombatifyState> state = Suppliers.memoize(() -> CombatifyState.COMBATIFY);
	public static boolean isLoaded = false;
	public static boolean mobConfigIsDirty = true;
	public static final List<Item> shields = new ArrayListExtensions<>();
	public static final List<UUID> unmoddedPlayers = new ArrayListExtensions<>();
	public static final List<UUID> moddedPlayers = new ArrayListExtensions<>();
	public static final Map<Holder<@NonNull Item>, ItemAttributeModifiers> originalModifiers = Util.make(new Object2ObjectOpenHashMap<>(), object2ObjectOpenHashMap -> object2ObjectOpenHashMap.defaultReturnValue(ItemAttributeModifiers.EMPTY));
	public static final Map<UUID, Boolean> isPlayerAttacking = new HashMap<>();
	public static final Map<String, WeaponType> defaultWeaponTypes = new HashMap<>();

	public static Identifier modDetectionNetworkChannel = id("networking");
	public static final Map<Identifier, BlockingType> defaultTypes = new HashMap<>();
	public static Map<Identifier, BlockingType> registeredTypes = new HashMap<>();
	public static final Identifier CHARGED_REACH_ID = id("charged_reach");

	public static final TagKey<@NonNull EntityType<?>> HAS_BOOSTED_SPEED = TagKey.create(Registries.ENTITY_TYPE, id("has_boosted_speed"));

	public static void markState(Supplier<CombatifyState> state) {
		Combatify.state = state;
	}

	public static CombatifyState getState() {
		return Combatify.state.get();
	}

	public static void init() {
		isLoaded = true;
		BlockingConditions.bootstrap();
		PostBlockEffects.bootstrap();
		WeaponType.init();
		networkingHandler = new NetworkingHandler();
		AttackEntityCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, pos, direction) -> {
			if (Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResult.PASS;
		});
		AttackBlockCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, pos, direction) -> {
			if (Combatify.unmoddedPlayers.contains(player.getUUID())) {
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
				HitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), direction, pos, false);
				hitResult = MethodHandler.redirectResult(player, hitResult);
				if (hitResult.getType() == HitResult.Type.ENTITY && player instanceof ServerPlayer serverPlayer) {
					//? >=26.1 {
					serverPlayer.connection.handleAttack(new ServerboundAttackPacket(((EntityHitResult) hitResult).getEntity().getId()));
					//?} <26.1 {
					/*serverPlayer.connection.handleInteract(ServerboundInteractPacket.createAttackPacket(((EntityHitResult) hitResult).getEntity(), player.isShiftKeyDown()));
					*///?}
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.PASS;
		});
		UseBlockCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, hitResult) -> {
			if (Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResult.PASS;
		});
		UseEntityCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, entity, hitResult) -> {
			if (Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResult.PASS;
		});
		UseItemCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand) -> {
			if (Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			//? >1.21.1 {
			return InteractionResult.PASS;
			//?} <=1.21.1 {
			/*return InteractionResultHolder.pass(player.getItemInHand(hand));
			*///?}
		});

		LOGGER.info("Init started.");
		//? fabric {
		CustomDataComponents.registerDataComponents();
		CustomEnchantmentEffectComponents.registerEnchantmentEffectComponents();
		DataComponentPredicateInit.registerDataComponentPredicates();
		BlockingTypeInit.init();
		if (FabricLoader.getInstance().isModLoaded("polymer-core"))
			polymerInit();
		CombatifyItemTags.init();
		if (CONFIG.dispensableTridents())
			DispenserBlock.registerProjectileBehavior(Items.TRIDENT);
		DefaultItemComponentEvents.MODIFY.register(modDetectionNetworkChannel, (modifyContext) -> {
			modifyContext.modify(Items.WOODEN_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL.get(), 1));
			modifyContext.modify(Items.GOLDEN_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL.get(), 1));
			modifyContext.modify(Items.STONE_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL.get(), 2));
			//? >1.21.9 {
			modifyContext.modify(Items.COPPER_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL.get(), 2));
			//?}
			modifyContext.modify(Items.IRON_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL.get(), 3));
			modifyContext.modify(Items.DIAMOND_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL.get(), 4));
			modifyContext.modify(Items.NETHERITE_SWORD, builder -> builder.set(CustomDataComponents.BLOCKING_LEVEL.get(), 5));
			modifyContext.modify(Items.SHIELD, builder -> builder.set(CustomDataComponents.EXTENDED_BLOCKING_DATA.get(), ExtendedBlockingData.VANILLA_SHIELD));
		});
		if (CONFIG.configOnlyWeapons()) {
			ItemRegistry.INSTANCE.init();
			//? >=26.1 {
			Event<CreativeModeTabEvents.@NonNull ModifyOutput> event = CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT);
			//?} <26.1 {
			/*Event<ItemGroupEvents.@NonNull ModifyEntries> event = ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT);
			*///?}
			event.register(entries -> insertAfter(entries, NETHERITE_SWORD, ItemRegistry.WOODEN_KNIFE.get(), ItemRegistry.STONE_KNIFE.get()/*? >=1.21.9 {*/, ItemRegistry.COPPER_KNIFE.get()/*?}*/, ItemRegistry.IRON_KNIFE.get(), ItemRegistry.GOLD_KNIFE.get(), ItemRegistry.DIAMOND_KNIFE.get(), ItemRegistry.NETHERITE_KNIFE.get(), ItemRegistry.WOODEN_LONGSWORD.get(), ItemRegistry.STONE_LONGSWORD.get()/*? >=1.21.9 {*/, ItemRegistry.COPPER_LONGSWORD.get()/*?}*/, ItemRegistry.IRON_LONGSWORD.get(), ItemRegistry.GOLD_LONGSWORD.get(), ItemRegistry.DIAMOND_LONGSWORD.get(), ItemRegistry.NETHERITE_LONGSWORD.get()));
		}
		if (CONFIG.tieredShields()) {
			TieredShieldBootstrap.INSTANCE.init();
			shields.add(Items.SHIELD);
			//? >=26.1 {
			Event<CreativeModeTabEvents.@NonNull ModifyOutput> event = CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT);
			 //?} <26.1 {
			/*Event<ItemGroupEvents.@NonNull ModifyEntries> event = ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT);
			*///?}
			event.register(entries -> insertAfter(entries, Items.SHIELD, TieredShieldBootstrap.IRON_SHIELD.get(), TieredShieldBootstrap.GOLD_SHIELD.get()/*? >=1.21.9 {*/, TieredShieldBootstrap.COPPER_SHIELD.get()/*?}*/, TieredShieldBootstrap.DIAMOND_SHIELD.get(), TieredShieldBootstrap.NETHERITE_SHIELD.get()));
		}

		CombatifyPatchGenerators.INSTANCE.init();
		ModContainer modContainer = FabricLoader.getInstance().getModContainer("combatify").get();
		ResourceManagerHelper.registerBuiltinResourcePack(id("alternate_mace"), modContainer, Component.translatable("pack.combatify.alternate_mace"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("combatify_extras"), modContainer, Component.translatable("pack.combatify.combatify_extras"), CONFIG.configOnlyWeapons() || CONFIG.tieredShields() ? ResourcePackActivationType.ALWAYS_ENABLED : ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("copper_age_rebalance"), modContainer, Component.translatable("pack.combatify.copper_age_rebalance"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("default_mace"), modContainer, Component.translatable("pack.combatify.default_mace"), ResourcePackActivationType.DEFAULT_ENABLED);
		ResourceManagerHelper.registerBuiltinResourcePack(id("default_shield"), modContainer, Component.translatable("pack.combatify.default_shield"), ResourcePackActivationType.DEFAULT_ENABLED);
		ResourceManagerHelper.registerBuiltinResourcePack(id("default_shield_attacker_kb"), modContainer, Component.translatable("pack.combatify.default_shield_attacker_knockback"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("old_sword_blocking"), modContainer, Component.translatable("pack.combatify.old_sword_blocking"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("percentage_shield"), modContainer, Component.translatable("pack.combatify.percentage_shield"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("shield_enchantments"), modContainer, Component.translatable("pack.combatify.shield_enchantments"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("shield_no_banner"), modContainer, Component.translatable("pack.combatify.shield_no_banner"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("sword_blocking"), modContainer, Component.translatable("pack.combatify.sword_blocking"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("vanilla_attack_balancing"), modContainer, Component.translatable("pack.combatify.vanilla_attack_balancing"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("weapon_tweaks"), modContainer, Component.translatable("pack.combatify.weapon_tweaks"), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(id("weapon_types"), modContainer, Component.translatable("pack.combatify.weapon_types"), ResourcePackActivationType.DEFAULT_ENABLED);
		ResourceManagerHelper.registerBuiltinResourcePack(id("wooden_shield_recipe"), modContainer, Component.translatable("pack.combatify.wooden_shield_recipe"), CONFIG.tieredShields() ? ResourcePackActivationType.DEFAULT_ENABLED : ResourcePackActivationType.NORMAL);
		if (Combatify.CONFIG.percentageDamageEffects())
			updateStrengthAndWeaknessModifiers(true);
		//?}
	}

	public static void updateStrengthAndWeaknessModifiers(boolean percentageDamageEffects) {
		AttributeModifier.Operation operation = percentageDamageEffects ? AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL : AttributeModifier.Operation.ADD_VALUE;
		//? >1.21.1 {
		MobEffects.STRENGTH
		//?} <=1.21.1 {
		/*MobEffects.DAMAGE_BOOST
		*///?}
			.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, IDUtils.withDefaultNamespace("effect.strength"), percentageDamageEffects ? 0.2 : 3, operation);
		MobEffects.WEAKNESS
			.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, IDUtils.withDefaultNamespace("effect.weakness"), percentageDamageEffects ? -0.2 : -4, operation);
	}

	//? >=26.1 {
	private static void insertAfter(FabricCreativeModeTabOutput entries,
	//?} <26.1 {
	/*private static void insertAfter(FabricItemGroupEntries entries,
	*///?}
									ItemLike after,
									ItemLike... toAppend) {
		//? >=26.1 {
		entries.insertAfter(after, toAppend);
		//?} <26.1 {
		/*entries.addAfter(after, toAppend);
		*///?}
	}

	public static void polymerInit() {
		//? >1.21.1 {
		PolymerItemUtils.CONTEXT_ITEM_CHECK.register((itemStack, packetContext) ->
		//?}
		//? <=1.21.1 {
		/*PolymerItemUtils.ITEM_CHECK.register(itemStack ->
		*///?}
			isPatched(/*? >=26.1 {*/ itemStack.typeHolder().value() /*?} <26.1 {*/ /*itemStack.getItem() *//*?}*/)
			|| itemStack.get(CustomDataComponents.EXTENDED_BLOCKING_DATA.get()) != null
			|| itemStack.get(CustomDataComponents.CAN_SWEEP.get()) != null
			|| itemStack.get(CustomDataComponents.BLOCKING_LEVEL.get()) != null
			|| itemStack.get(CustomDataComponents.PIERCING_LEVEL.get()) != null
			|| itemStack.get(CustomDataComponents.CHARGED_REACH.get()) != null);
		PolymerItemUtils.ITEM_MODIFICATION_EVENT.register(
			(itemStack,
			 itemStack1,
			 //? >1.21.1 {
			 packetContext
			 //?} <= 1.21.1 {
			 /*player
			 *///?}
		) -> {
			//? >1.21.1 {
			if (packetContext == null) return itemStack1;
			ServerPlayer player = packetContext.get(PacketContext.SERVER_INSTANCE).getPlayerList().getPlayer(packetContext.get(PacketContext.GAME_PROFILE).id());
			//?}
			if (player == null || moddedPlayers.contains(player.getUUID())) {
				if (itemStack.has(CustomDataComponents.EXTENDED_BLOCKING_DATA.get()))
					itemStack1.set(CustomDataComponents.EXTENDED_BLOCKING_DATA.get(), itemStack.get(CustomDataComponents.EXTENDED_BLOCKING_DATA.get()));
				if (itemStack.has(CustomDataComponents.CAN_SWEEP.get()))
					itemStack1.set(CustomDataComponents.CAN_SWEEP.get(), itemStack.get(CustomDataComponents.CAN_SWEEP.get()));
				if (itemStack.has(CustomDataComponents.BLOCKING_LEVEL.get()))
					itemStack1.set(CustomDataComponents.BLOCKING_LEVEL.get(), itemStack.get(CustomDataComponents.BLOCKING_LEVEL.get()));
				if (itemStack.has(CustomDataComponents.PIERCING_LEVEL.get()))
					itemStack1.set(CustomDataComponents.PIERCING_LEVEL.get(), itemStack.get(CustomDataComponents.PIERCING_LEVEL.get()));
				if (itemStack.has(CustomDataComponents.CHARGED_REACH.get()))
					itemStack1.set(CustomDataComponents.CHARGED_REACH.get(), itemStack.get(CustomDataComponents.CHARGED_REACH.get()));
			}
			return itemStack1;
		});
	}

	public static void setDurability(DataComponentPatch.Builder builder, @NonNull Item item, int value) {
		builder.set(DataComponents.DAMAGE, 0);
		builder.set(DataComponents.MAX_DAMAGE, value);
		builder.set(DataComponents.MAX_STACK_SIZE, 1);
	}

	public static BlockingType registerBlockingType(BlockingType blockingType) {
		Combatify.registeredTypes.put(blockingType.name(), blockingType);
		return blockingType;
	}

	// Oh yeah baby
	//? >=1.21.11 {
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	//?} < 1.21.11 {
	/*public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
	*///?}

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

	public static boolean isStateVanilla() {
		return getState().equals(CombatifyState.VANILLA);
	}

	static {
		FoodImpl.bootstrap();
		CritImpl.bootstrap();
		CONFIG = new CombatifyGeneralConfig();
	}
}
