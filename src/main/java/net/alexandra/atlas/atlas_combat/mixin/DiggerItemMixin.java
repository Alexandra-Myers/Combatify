package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

import static net.alexandra.atlas.atlas_combat.item.WeaponType.BASE_BLOCK_REACH_UUID;

@Mixin(DiggerItem.class)
public class DiggerItemMixin extends TieredItem implements Vanishable {
	public DiggerItemMixin(Tier tier, Item.Properties properties) {
		super(tier, properties);
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;build()Lcom/google/common/collect/ImmutableMultimap;"))
	public ImmutableMultimap test(ImmutableMultimap.Builder instance) {
		ImmutableMultimap.Builder var3 = ImmutableMultimap.builder();
		if((Object)this instanceof AxeItem) {
			WeaponType.AXE.addCombatAttributes(this.getTier(), var3);
			return var3.build();
		}else if((Object)this instanceof PickaxeItem) {
			WeaponType.PICKAXE.addCombatAttributes(this.getTier(), var3);
			return var3.build();
		}else if((Object)this instanceof ShovelItem) {
			WeaponType.SHOVEL.addCombatAttributes(this.getTier(), var3);
			return var3.build();
		}else if((Object)this instanceof HoeItem) {
			WeaponType.HOE.addCombatAttributes(this.getTier(), var3);
			return var3.build();
		}
		return var3.build();
	}
	@Redirect(method = "hurtEnemy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
	public <T extends LivingEntity> void damage(ItemStack instance, int amount, T entity, Consumer<T> breakCallback) {
		if (!entity.level.isClientSide && (!(entity instanceof Player) || !((Player)entity).getAbilities().invulnerable)) {
			if (instance.isDamageableItem()) {
				if((Object)this instanceof AxeItem || (Object) this instanceof HoeItem) {
					if (instance.hurt(1, entity.getRandom(), entity instanceof ServerPlayer ? (ServerPlayer) entity : null)) {
						breakCallback.accept(entity);
						Item item = instance.getItem();
						instance.shrink(1);
						if (entity instanceof Player) {
							((Player) entity).awardStat(Stats.ITEM_BROKEN.get(item));
						}

						instance.setDamageValue(0);
					}
				}else{
					if (instance.hurt(amount, entity.getRandom(), entity instanceof ServerPlayer ? (ServerPlayer) entity : null)) {
						breakCallback.accept(entity);
						Item item = instance.getItem();
						instance.shrink(1);
						if (entity instanceof Player) {
							((Player) entity).awardStat(Stats.ITEM_BROKEN.get(item));
						}

						instance.setDamageValue(0);
					}
				}
			}
		}
	}
}
