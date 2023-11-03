package net.atlas.combatify.mixin.accessors;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(Item.class)
public interface ItemAccessor {

	@Accessor("BASE_ATTACK_DAMAGE_UUID")
	static UUID getBaseAttackDamageUUID() {
		throw new AssertionError();
	}

	@Accessor("BASE_ATTACK_SPEED_UUID")
	static UUID getBaseAttackSpeedUUID() {
		throw new AssertionError();
	}

	@Accessor("maxStackSize")
	@Mutable
	void combatify$setMaxStackSize(int maxStackSize);
}
