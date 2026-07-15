package net.atlas.combatify.config.impl.crit;

import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.serialization.MapCodec;
import net.atlas.combatify.config.impl.JSImpl;
import net.atlas.combatify.config.wrapper.EntityWrapper;
import net.atlas.combatify.config.wrapper.PlayerWrapper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record JSCritImpl(JSImpl handler) implements CritImpl {
	public static final Identifier ID = Identifier.withDefaultNamespace("javascript");
	public static final MapCodec<JSCritImpl> CODEC = JSImpl.CODEC.fieldOf("script").xmap(JSCritImpl::new, JSCritImpl::handler);

	@Override
	public boolean overrideCrit() {
		return handler.execFunc("overrideCrit()");
	}

	@Override
	public boolean runCrit(Player attacker, Entity target, LocalFloatRef damageRef) {
		return handler.execFunc("runCrit(player, target, combinedDamage)", new PlayerWrapper<>(attacker), EntityWrapper.of(target), damageRef);
	}

	@Override
	public MapCodec<? extends CritImpl> type() {
		return CODEC;
	}
}
