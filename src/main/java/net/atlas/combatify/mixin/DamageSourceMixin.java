package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.DamageSourceExtension;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceExtension {
	@Unique
	public boolean originatedFromBlockedAttack = false;

	@Override
	public boolean combatify$originatedFromBlockedAttack() {
		return originatedFromBlockedAttack;
	}

	@Override
	public DamageSource combatify$originatesFromBlockedAttack(boolean origin) {
		originatedFromBlockedAttack = origin;
		return DamageSource.class.cast(this);
	}
}
