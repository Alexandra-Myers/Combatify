package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.combatify.Combatify;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(Tiers.class)
public abstract class TiersMixin {
	@WrapMethod(method = "<init>")
	private void modifyDamage(String string, int i, TagKey<Block> tagKey, int j, float f, float g, int k, Supplier<Ingredient> supplier, Operation<Void> original) {
		if (g > 0 && Combatify.CONFIG.tierDamageNerf()) {
			g -= 1;
		}
		original.call(string, i, tagKey, j, f, g, k, supplier);
	}
}
