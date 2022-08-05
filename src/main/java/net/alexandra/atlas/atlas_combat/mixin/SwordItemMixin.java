package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwordItem.class)
public class SwordItemMixin extends TieredItem {

	@Shadow
	private Multimap<Attribute, AttributeModifier> defaultModifiers;

	public SwordItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
		ImmutableMultimap.Builder var3 = ImmutableMultimap.builder();
		WeaponType.SWORD.addCombatAttributes(this.getTier(), var3);
		defaultModifiers = var3.build();
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public float getDamage() {
		return WeaponType.SWORD.getDamage(this.getTier());
	}
}
