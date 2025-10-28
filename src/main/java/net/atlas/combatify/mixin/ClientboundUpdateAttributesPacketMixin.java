package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.IUpdateAttributesPacket;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

import static net.atlas.combatify.item.WeaponType.BASE_ATTACK_SPEED_UUID;

@Mixin(ClientboundUpdateAttributesPacket.class)
public class ClientboundUpdateAttributesPacketMixin implements IUpdateAttributesPacket {
	@Shadow
	@Final
	private List<ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes;

	@Override
	public void changeAttributes(ServerPlayer reciever) {
		List<Integer> indexes = new ArrayList<>();
		Map<Integer, AttributeModifier> modifierMap = new HashMap<>();
		for (ClientboundUpdateAttributesPacket.AttributeSnapshot attributeSnapshot : attributes) {
			if (attributeSnapshot.getAttribute() == Attributes.ATTACK_SPEED && Combatify.unmoddedPlayers.contains(reciever.getUUID())) {
				double speed = calculateValue(attributeSnapshot.getBase(), attributeSnapshot.getModifiers(), attributeSnapshot.getAttribute());
				boolean hasVanilla = Combatify.getState().equals(Combatify.CombatifyState.VANILLA) || (!attributeSnapshot.getModifiers().stream()
					.filter(attributeModifier -> attributeModifier.getId() == Item.BASE_ATTACK_SPEED_UUID)
					.toList()
					.isEmpty() && !Combatify.getState().equals(Combatify.CombatifyState.CTS_8C));
				for (double newSpeed = speed - 1.5; newSpeed > 0; newSpeed -= 0.001) {
					if (vanillaMath(newSpeed) == CTSMath(speed, hasVanilla) * 2) {
						if (newSpeed - 2.5 != 0)
							modifierMap.put(attributes.indexOf(attributeSnapshot), new AttributeModifier(WeaponType.BASE_ATTACK_SPEED_CTS_UUID, "Calculated client modifier", newSpeed - 2.5, AttributeModifier.Operation.ADDITION));
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
				attributes.add(index, new ClientboundUpdateAttributesPacket.AttributeSnapshot(attributeSnapshot.getAttribute(), attributeSnapshot.getBase() - 1.5, newModifiers));
			}
	}
	public final double calculateValue(double baseValue, Collection<AttributeModifier> modifiers, Attribute attribute) {
		double attributeInstanceBaseValue = baseValue;
		List<AttributeModifier> additionList = modifiers
			.stream()
			.filter(attributeModifier -> attributeModifier.getOperation() == AttributeModifier.Operation.ADDITION)
			.toList();
		List<AttributeModifier> multiplyBaseList = modifiers
			.stream()
			.filter(attributeModifier -> attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
			.toList();
		List<AttributeModifier> multiplyTotalList = modifiers
			.stream()
			.filter(attributeModifier -> attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
			.toList();

		for(AttributeModifier attributeModifier : additionList) {
			attributeInstanceBaseValue += attributeModifier.getAmount();
		}

		for(AttributeModifier attributeModifier2 : multiplyBaseList) {
			attributeInstanceBaseValue += attributeInstanceBaseValue * attributeModifier2.getAmount();
		}

		for(AttributeModifier attributeModifier2 : multiplyTotalList) {
			attributeInstanceBaseValue *= 1.0 + attributeModifier2.getAmount();
		}

		return attribute.sanitizeValue(attributeInstanceBaseValue);
	}
	private static int CTSMath(double attackSpeed, boolean hasVanilla) {
		double d = attackSpeed - 1.5;
		if (hasVanilla || d <= 0)
			d += 1.5;
		d = 1.0 / d * 20.0 + (hasVanilla ? 0 : 0.5);
		return (int) (d);
	}
	private static int vanillaMath(double attackSpeed) {
		double d = attackSpeed;
		d = 1.0 / d * 20.0;
		return (int) (d);
	}
}
