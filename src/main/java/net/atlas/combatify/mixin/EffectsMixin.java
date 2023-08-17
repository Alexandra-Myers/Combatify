package net.atlas.combatify.mixin;

import com.mojang.serialization.Lifecycle;
import net.atlas.combatify.util.DummyAttackDamageMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MobEffects.class)
public class EffectsMixin {
	@Shadow
	@Mutable
	@Final
	public static MobEffect DAMAGE_BOOST = registrySet(
			5,
			"strength",
			new DummyAttackDamageMobEffect(MobEffectCategory.BENEFICIAL, 9643043, 0.2)
					.addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
	@Shadow
	@Mutable
	@Final
	public static final MobEffect WEAKNESS = registrySet(
			18,
			"weakness",
			new DummyAttackDamageMobEffect(MobEffectCategory.HARMFUL, 4738376, -0.2)
					.addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
	);
	private static MobEffect registrySet(int RawId, String id, MobEffect effect) {
		Holder<MobEffect> mobEffectHolder = ((WritableRegistry<MobEffect>) BuiltInRegistries.MOB_EFFECT).registerMapping(RawId, ResourceKey.create(BuiltInRegistries.MOB_EFFECT.key(), new ResourceLocation(id)), effect, Lifecycle.stable());
		return mobEffectHolder.value();
	}
}
