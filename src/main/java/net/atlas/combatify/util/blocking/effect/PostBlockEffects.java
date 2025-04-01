package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.extension.LateBoundIdMapper;
import net.minecraft.resources.ResourceLocation;

public class PostBlockEffects {
	public static final LateBoundIdMapper<ResourceLocation, MapCodec<? extends PostBlockEffect>> ID_MAPPER = new LateBoundIdMapper<>();
	public static final MapCodec<PostBlockEffect> MAP_CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
		.dispatchMap("effect", PostBlockEffect::type, mapCodec -> mapCodec);

	public static void bootstrap() {
		ID_MAPPER.put(DoNothing.ID, DoNothing.MAP_CODEC);
		ID_MAPPER.put(KnockbackEntity.ID, KnockbackEntity.MAP_CODEC);
		ID_MAPPER.put(HurtEntity.ID, HurtEntity.MAP_CODEC);
		ID_MAPPER.put(ApplyEffect.ID, ApplyEffect.MAP_CODEC);
		ID_MAPPER.put(EnchantmentEffect.ID, EnchantmentEffect.MAP_CODEC);
		ID_MAPPER.put(RunFunction.ID, RunFunction.MAP_CODEC);
		ID_MAPPER.put(AllOf.ID, AllOf.MAP_CODEC);
	}
}
