package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.IUpdateAttributesPacket;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@Mixin(ClientboundUpdateAttributesPacket.class)
public class ClientboundUpdateAttributesPacketMixin implements IUpdateAttributesPacket {
	@Shadow
	@Final
	private List<ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes;

	@Override
	public void combatify$changeAttributes(ServerPlayer reciever) {
		List<Integer> indexes = new ArrayList<>();
		Map<Integer, AttributeModifier> modifierMap = new HashMap<>();
		for (ClientboundUpdateAttributesPacket.AttributeSnapshot attributeSnapshot : attributes) {
			if (attributeSnapshot.attribute() == Attributes.ATTACK_SPEED) {
				double speed = calculateValue(attributeSnapshot.base(), attributeSnapshot.modifiers(), attributeSnapshot.attribute());
				double mod = !Combatify.CONFIG.hasteFix() ? 1.5 : calculateValueFromBase(1.5, attributeSnapshot.modifiers(), attributeSnapshot.attribute());
				boolean hasVanilla = !attributeSnapshot.modifiers().stream()
					.filter(attributeModifier -> attributeModifier.id().equals(Item.BASE_ATTACK_SPEED_ID))
					.toList()
					.isEmpty() && !Combatify.isCTS;
				int mul = Combatify.CONFIG.chargedAttacks() ? 2 : 1;
				for (double newSpeed = speed - mod; newSpeed > 0; newSpeed -= 0.0001) {
					if (vanillaMath(newSpeed / mul) == CTSMath(speed, hasVanilla, mod) * mul) {
						modifierMap.put(attributes.indexOf(attributeSnapshot), new AttributeModifier(WeaponType.BASE_ATTACK_SPEED_CTS_ID, (newSpeed / mul) - 2.5, AttributeModifier.Operation.ADD_VALUE));
						break;
					}
					if (vanillaMath(newSpeed) == CTSMath(speed, hasVanilla, mod) * mul) {
						modifierMap.put(attributes.indexOf(attributeSnapshot), new AttributeModifier(WeaponType.BASE_ATTACK_SPEED_CTS_ID, newSpeed - 2.5, AttributeModifier.Operation.ADD_VALUE));
						break;
					}
				}
				indexes.add(attributes.indexOf(attributeSnapshot));
			}
		}
		if (!indexes.isEmpty())
			for (Integer index : indexes) {
				AttributeModifier modifierToSend = modifierMap.get(index);
				Collection<AttributeModifier> newModifiers = Collections.singletonList(modifierToSend);
				ClientboundUpdateAttributesPacket.AttributeSnapshot attributeSnapshot = attributes.remove(index.intValue());
				attributes.add(index, new ClientboundUpdateAttributesPacket.AttributeSnapshot(attributeSnapshot.attribute(), attributeSnapshot.base() - 1.5, newModifiers));
			}
	}
	@Unique
	public final double calculateValue(double baseValue, Collection<AttributeModifier> modifiers, Holder<Attribute> attribute) {
		double attributeInstanceBaseValue = baseValue;
		List<AttributeModifier> additionList = modifiers
			.stream()
			.filter(attributeModifier -> attributeModifier.operation() == AttributeModifier.Operation.ADD_VALUE)
			.toList();

		for(AttributeModifier attributeModifier : additionList) {
			attributeInstanceBaseValue += attributeModifier.amount();
		}

		return calculateValueFromBase(attributeInstanceBaseValue, modifiers, attribute);
	}
	@Unique
	public final double calculateValueFromBase(double attributeInstanceBaseValue, Collection<AttributeModifier> modifiers, Holder<Attribute> attribute) {
		List<AttributeModifier> multiplyBaseList = modifiers
			.stream()
			.filter(attributeModifier -> attributeModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
			.toList();
		List<AttributeModifier> multiplyTotalList = modifiers
			.stream()
			.filter(attributeModifier -> attributeModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
			.toList();

		double attributeInstanceFinalValue = attributeInstanceBaseValue;

		for(AttributeModifier attributeModifier2 : multiplyBaseList) {
			attributeInstanceFinalValue += attributeInstanceBaseValue * attributeModifier2.amount();
		}

		for(AttributeModifier attributeModifier2 : multiplyTotalList) {
			attributeInstanceFinalValue *= 1.0 + attributeModifier2.amount();
		}

		return attribute.value().sanitizeValue(attributeInstanceFinalValue);
	}
	@Unique
	private static int CTSMath(double attackSpeed, boolean hasVanilla, double mod) {
		double d = attackSpeed - mod;
		if (hasVanilla || d < 0)
			d += mod;
		d = Mth.clamp(d, 0.1, 1024.0);
		d = 1.0 / d * 20.0 + (hasVanilla ? 0 : 0.5);
		return (int) (d);
	}
	@Unique
	private static int vanillaMath(double attackSpeed) {
		return (int) (1.0 / attackSpeed * 20.0);
	}
}
