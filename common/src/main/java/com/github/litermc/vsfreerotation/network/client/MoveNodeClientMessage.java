package com.github.litermc.vsfreerotation.network.client;

import com.github.litermc.vsfreerotation.network.MessageType;
import com.github.litermc.vsfreerotation.network.NetworkMessage;
import com.github.litermc.vsfreerotation.network.NetworkMessages;
import com.github.litermc.vsfreerotation.util.SerializeUtil;

import net.minecraft.network.FriendlyByteBuf;

import org.joml.Quaternionfc;
import org.joml.Vector3dc;

public final class MoveNodeClientMessage implements NetworkMessage<ClientNetworkContext> {
	private final int id;
	private final Vector3dc position;
	private final Quaternionfc rotation;

	public MoveNodeClientMessage(final int id, final Vector3dc position, final Quaternionfc rotation) {
		this.id = id;
		this.position = position;
		this.rotation = rotation;
	}

	public MoveNodeClientMessage(final FriendlyByteBuf buf) {
		this.id = buf.readVarInt();
		this.position = SerializeUtil.readVector3d(buf);
		this.rotation = SerializeUtil.readQuaternionf(buf);
	}

	@Override
	public void write(final FriendlyByteBuf buf) {
		buf.writeVarInt(this.id);
		SerializeUtil.writeVector3d(buf, this.position);
		SerializeUtil.writeQuaternionf(buf, this.rotation);
	}

	@Override
	public void handle(final ClientNetworkContext context) {
		context.moveNode(this.id, this.position, this.rotation);
	}

	@Override
	public MessageType<MoveNodeClientMessage> type() {
		return NetworkMessages.MOVE_NODE;
	}
}
