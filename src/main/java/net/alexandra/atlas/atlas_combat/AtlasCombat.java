package net.alexandra.atlas.atlas_combat;

import com.mojang.serialization.Lifecycle;
import io.netty.buffer.Unpooled;
import net.alexandra.atlas.atlas_combat.config.ConfigHelper;
import net.alexandra.atlas.atlas_combat.enchantment.CleavingEnchantment;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.networking.NetworkingHandler;
import net.alexandra.atlas.atlas_combat.util.DummyAttackDamageMobEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class AtlasCombat implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Atlas Combat");

	public static ConfigHelper helper = new ConfigHelper();
	private static final Component ACCESSIBILITY_TOOLTIP_LOW_SHIELD = Component.translatable("options.lowShield.tooltip");
	public static final OptionInstance<Boolean> autoAttack = OptionInstance.createBoolean("options.autoAttack", true);
	public static final OptionInstance<Boolean> shieldCrouch = OptionInstance.createBoolean("options.shieldCrouch", true);
	public static final OptionInstance<Boolean> lowShield = OptionInstance.createBoolean("options.lowShield", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_LOW_SHIELD),false);

	public static final CleavingEnchantment CLEAVING_ENCHANTMENT = register();
	public static CleavingEnchantment register() {
		return Registry.register(Registry.ENCHANTMENT, new ResourceLocation("atlas_combat","cleaving"),new CleavingEnchantment());
	}

	@Override
	public void onInitialize(ModContainer mod) {

		NetworkingHandler networkingHandler = new NetworkingHandler();

		DispenserBlock.registerBehavior(Items.TRIDENT, new AbstractProjectileDispenseBehavior() {
			@Override
			protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
				ThrownTrident trident = new ThrownTrident(EntityType.TRIDENT, world);
				trident.tridentItem = stack.copy();
				trident.setPosRaw(position.x(), position.y(), position.z());
				trident.pickup = AbstractArrow.Pickup.ALLOWED;
				return trident;
			}
		});

		((WritableRegistry) Registry.MOB_EFFECT).registerOrOverride(OptionalInt.of(5),
				ResourceKey.create(Registry.MOB_EFFECT.key(), new ResourceLocation("strength")),
				new DummyAttackDamageMobEffect(MobEffectCategory.BENEFICIAL, 9643043, 3.0)
						.addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9",
								1.2, AttributeModifier.Operation.MULTIPLY_TOTAL),
				Lifecycle.stable());

		List<Item> items = Registry.ITEM.stream().toList();

		for(Item item : items) {
			int newStackSize = helper.itemsJsonElement.getAsJsonObject().get(item.toString()).getAsInt();

			if(item.maxStackSize == newStackSize) continue;

			((ItemExtensions)item).setStackSize(helper.itemsJsonElement.getAsJsonObject().get(item.toString()).getAsInt());

			Map<ItemStack,Integer> changedItems = new HashMap<>();
			changedItems.put(item.getDefaultInstance(),newStackSize);

			ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
				for(Map.Entry<ItemStack,Integer> entrySet : changedItems.entrySet()) {
					FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
					buf.writeItem(entrySet.getKey());
					buf.writeInt(entrySet.getValue());
					ServerPlayNetworking.send(handler.player,networkingHandler.itemStackSizeNetworkChannel,buf);
				}
			});
		}

	}
}
