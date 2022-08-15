package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.serialization.Lifecycle;
import net.alexandra.atlas.atlas_combat.util.DummyAttackDamageMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.AttackDamageMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalInt;

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
	private static MobEffect registrySet(int RawId, String id, MobEffect effect) {
		Holder<MobEffect> mobEffectHolder = ((WritableRegistry) Registry.MOB_EFFECT).registerOrOverride(OptionalInt.of(RawId), ResourceKey.create(Registry.ATTRIBUTE.key(), new ResourceLocation(id)), effect, Lifecycle.stable());
		return mobEffectHolder.value();
	}
}
