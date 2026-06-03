package net.atlas.combatify.config.impl.crit;

import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.extension.LateBoundIdMapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public interface CritImpl {
	boolean overrideCrit();
	boolean runCrit(Player attacker, Entity target, LocalFloatRef damageRef);

	MapCodec<? extends CritImpl> type();
	LateBoundIdMapper<@NotNull ResourceLocation, @NotNull MapCodec<? extends CritImpl>> ID_MAPPER = new LateBoundIdMapper<>();
	Codec<CritImpl> CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
		.dispatch(CritImpl::type, mapCodec -> mapCodec);

	static void bootstrap() {
		ID_MAPPER.put(JSCritImpl.ID, JSCritImpl.CODEC);
		ID_MAPPER.put(CTSCritImpl.ID, CTSCritImpl.CODEC);
		ID_MAPPER.put(CombatifyCritImpl.ID, CombatifyCritImpl.CODEC);
	}
}
