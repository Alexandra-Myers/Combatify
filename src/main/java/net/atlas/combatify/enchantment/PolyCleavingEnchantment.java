package net.atlas.combatify.enchantment;

import eu.pb4.polymer.core.api.other.PolymerEnchantment;
import net.atlas.combatify.Combatify;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

public class PolyCleavingEnchantment extends CleavingEnchantment implements PolymerEnchantment {
	@Override
	public @Nullable Enchantment getPolymerReplacement(ServerPlayer player) {
		if (Combatify.unmoddedPlayers.contains(player.getUUID()))
			return Enchantments.SHARPNESS;
		return this;
	}
}
