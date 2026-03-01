package net.atlas.combatify.config.impl.crit;

import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public interface CritImpl {
	boolean overrideCrit();
	boolean runCrit(Player attacker, Entity target, LocalFloatRef damageRef);

	MapCodec<? extends CritImpl> type();
	ExtraCodecs.LateBoundIdMapper<@NotNull Identifier, @NotNull MapCodec<? extends CritImpl>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
	Codec<CritImpl> CODEC = ID_MAPPER.codec(Identifier.CODEC)
		.dispatch(CritImpl::type, mapCodec -> mapCodec);

	static void bootstrap() {
		ID_MAPPER.put(JSCritImpl.ID, JSCritImpl.CODEC);
		ID_MAPPER.put(CTSCritImpl.ID, CTSCritImpl.CODEC);
		ID_MAPPER.put(CombatifyCritImpl.ID, CombatifyCritImpl.CODEC);
	}
}
