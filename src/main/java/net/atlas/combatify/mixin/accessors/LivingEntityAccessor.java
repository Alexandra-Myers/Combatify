package net.atlas.combatify.mixin.accessors;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

	@Accessor("attackStrengthTicker")
	int getAttackStrengthTicker();

	@Accessor("attackStrengthTicker")
    void setAttackStrengthTicker(int ticket);

	@Invoker("blockUsingShield")
	void blockUsingShield(LivingEntity entity);

}
