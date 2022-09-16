package net.alexandra.atlas.atlas_combat.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

public class NewActionTypeStruct extends EnchantmentTargetMixin {

}

@Mixin(targets = "net/minecraft/network/protocol/game/ServerboundInteractPacket$ActionType")
abstract class EnchantmentTargetMixin {
}
