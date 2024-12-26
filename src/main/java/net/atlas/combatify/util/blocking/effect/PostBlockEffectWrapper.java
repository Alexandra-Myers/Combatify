package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public record PostBlockEffectWrapper(EnchantmentTarget affected, PostBlockEffect effect, Optional<LootItemCondition> requirements) {
	public static final PostBlockEffectWrapper DEFAULT = new PostBlockEffectWrapper(EnchantmentTarget.ATTACKER, new DoNothing(), Optional.empty());
	public static final PostBlockEffectWrapper KNOCKBACK = new PostBlockEffectWrapper(EnchantmentTarget.ATTACKER, new KnockbackEntity(), Optional.empty());
	public static final MapCodec<PostBlockEffectWrapper> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				EnchantmentTarget.CODEC.optionalFieldOf("affected", EnchantmentTarget.ATTACKER).forGetter(PostBlockEffectWrapper::affected),
				PostBlockEffects.MAP_CODEC.forGetter(PostBlockEffectWrapper::effect),
				ConditionalEffect.conditionCodec(LootContextParamSets.ENCHANTED_DAMAGE).optionalFieldOf("requirements").forGetter(PostBlockEffectWrapper::requirements)
			)
			.apply(instance, PostBlockEffectWrapper::new)
	);

	public boolean matches(LootContext lootContext) {
		return this.requirements.map(lootItemCondition -> lootItemCondition.test(lootContext)).orElse(true);
	}
}
