package com.github.litermc.vsfreerotation.network.server;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeLivingEntity;
import com.github.litermc.vsfreerotation.network.MessageType;
import com.github.litermc.vsfreerotation.network.NetworkMessage;
import com.github.litermc.vsfreerotation.network.NetworkMessages;
import com.github.litermc.vsfreerotation.util.SerializeUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.joml.Quaternionfc;
import org.joml.Vector3dc;

public final class PlayerMoveServerMessage implements NetworkMessage<ServerNetworkContext> {
	private final Vector3dc position;
	private final Quaternionfc rotation;
	private final float headPitch;
	private final float headYaw;

	public PlayerMoveServerMessage(final Vector3dc position, final Quaternionfc rotation, final float headPitch, final float headYaw) {
		this.position = position;
		this.rotation = rotation;
		this.headPitch = headPitch;
		this.headYaw = headYaw;
	}

	public PlayerMoveServerMessage(final FriendlyByteBuf buf) {
		this.position = SerializeUtil.readVector3d(buf);
		this.rotation = SerializeUtil.readQuaternionf(buf);
		this.headPitch = buf.readFloat();
		this.headYaw = buf.readFloat();
	}

	@Override
	public void write(final FriendlyByteBuf buf) {
		SerializeUtil.writeVector3d(buf, this.position);
		SerializeUtil.writeQuaternionf(buf, this.rotation);
		buf.writeFloat(this.headPitch);
		buf.writeFloat(this.headYaw);
	}

	@Override
	public void handle(final ServerNetworkContext context) {
		ServerPlayer player = context.getSender();
		if (player == null || !(((EntityAccessor) player).vsfr$getFreeEntity() instanceof final FreeLivingEntity<?> fe)) {
			return;
		}
		fe.setPosition(this.position);
		fe.setRotation(this.rotation);
		fe.setHeadPitch(this.headPitch);
		fe.setHeadYaw(this.headYaw);
	}

	@Override
	public MessageType<PlayerMoveServerMessage> type() {
		return NetworkMessages.PLAYER_MOVE;
	}
}
