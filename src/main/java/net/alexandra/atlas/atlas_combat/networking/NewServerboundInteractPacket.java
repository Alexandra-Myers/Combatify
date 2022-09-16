package net.alexandra.atlas.atlas_combat.networking;

import com.chocohead.mm.api.ClassTinkerers;
import net.alexandra.atlas.atlas_combat.extensions.IHandler;
import net.alexandra.atlas.atlas_combat.extensions.IServerboundInteractPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class NewServerboundInteractPacket extends ServerboundInteractPacket implements IServerboundInteractPacket {
	public static final ServerboundInteractPacket.Action MISS_ATTACK_ACTION = new ServerboundInteractPacket.Action() {
		ServerboundInteractPacket.ActionType type = ClassTinkerers.getEnum(ServerboundInteractPacket.ActionType.class, "MISS_ATTACK");
		@Override
		public ServerboundInteractPacket.ActionType getType() {
			return type;
		}

		@Override
		public void dispatch(ServerboundInteractPacket.Handler handler) {
			((IHandler)handler).onMissAttack();
		}

		@Override
		public void write(FriendlyByteBuf buf) {
		}
	};
	public NewServerboundInteractPacket(int i, boolean bl, ServerboundInteractPacket.Action action) {
		super(i, bl, action);
	}
	public static ServerboundInteractPacket createMissPacket(int i, boolean bl) {
		return new ServerboundInteractPacket(i, bl, MISS_ATTACK_ACTION);
	}
}
