package net.atlas.combatify.compat;

import net.atlas.combatify.enchantment.CleavingEnchantment;
import net.atlas.combatify.enchantment.PolyCleavingEnchantment;

public class PolymerCompat {
	public static CleavingEnchantment getPolyCleaving() {
		return new PolyCleavingEnchantment();
	}
}
