package net.atlas.combatify.util.blocking.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record ChangeItemDamage(LevelBasedValue amount) implements PostBlockEffect {
	public static final Identifier ID = Identifier.withDefaultNamespace("change_item_damage");
	public static final MapCodec<ChangeItemDamage> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter(changeItemDamage -> changeItemDamage.amount))
			.apply(instance, ChangeItemDamage::new)
	);

	@Override
	public void doEffect(ServerLevel serverLevel, EnchantedItemInUse enchantedItemInUse, LivingEntity attacker, DamageSource damageSource, int enchantmentLevel, LivingEntity toApply, Vec3 position) {
		ItemStack itemStack = enchantedItemInUse.itemStack();
		if (itemStack.has(DataComponents.MAX_DAMAGE) && itemStack.has(DataComponents.DAMAGE)) {
			ServerPlayer serverPlayer2 = enchantedItemInUse.owner() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
			int calculated = (int) this.amount.calculate(enchantmentLevel);
			itemStack.hurtAndBreak(calculated, serverLevel, serverPlayer2, enchantedItemInUse.onBreak());
		}
	}

	@Override
	public MapCodec<? extends PostBlockEffect> type() {
		return MAP_CODEC;
	}

	@Override
	public Identifier id() {
		return ID;
	}
}
