package net.atlas.combatify.networking;

import commonnetwork.networking.data.PacketContext;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.extensions.ItemExtensions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import static net.atlas.combatify.Combatify.*;

public class ItemConfigPacket {
	public static final ResourceLocation CHANNEL = id("item_config");

	public ItemConfigPacket(ItemConfig config) {
	}

	public static ItemConfigPacket decode(FriendlyByteBuf buf) {
		return new ItemConfigPacket(ITEMS.loadFromNetwork(buf));
	}

	public void encode(FriendlyByteBuf buf) {
		ITEMS.saveToNetwork(buf);
	}

	public static void handle(PacketContext<ItemConfigPacket> ctx) {
		LOGGER.info("Loading config details from buffer.");

		IForgeRegistry<Item> items = ForgeRegistries.ITEMS;

		for(Item item : items)
			((ItemExtensions) item).modifyAttributeModifiers();
		for (Item item : ITEMS.configuredItems.keySet()) {
			ConfigurableItemData configurableItemData = ITEMS.configuredItems.get(item);
			if (configurableItemData.stackSize != null)
				((ItemExtensions) item).setStackSize(configurableItemData.stackSize);
		}
	}
}
