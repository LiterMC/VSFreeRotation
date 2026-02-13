package com.github.litermc.vsfreerotation.network.client;

import com.github.litermc.vsfreerotation.network.MessageType;
import com.github.litermc.vsfreerotation.network.NetworkMessage;
import com.github.litermc.vsfreerotation.network.NetworkMessages;
import com.github.litermc.vsfreerotation.util.SerializeUtil;

import net.minecraft.network.FriendlyByteBuf;

import org.joml.Quaternionfc;
import org.joml.Vector3dc;

public final class RelocateNodeClientMessage implements NetworkMessage<ClientNetworkContext> {
	private final int id;
	private final int anchorId;
	private final Vector3dc pos;
	private final Quaternionfc rotation;

	public RelocateNodeClientMessage(final int id, final int anchorId, final Vector3dc pos, final Quaterniond rotation) {
		this.id = id;
		this.anchorId = anchorId;
		this.pos = pos;
		this.rotation = rotation;
	}

	public RelocateNodeClientMessage(final FriendlyByteBuf buf) {
		this.id = buf.readVarInt();
		this.anchorId = buf.readVarInt();
		this.pos = SerializeUtil.readVector3d(buf);
		this.rotation = SerializeUtil.readQuaternionf(buf);
	}

	@Override
	public void write(final FriendlyByteBuf buf) {
		buf.writeVarInt(this.id);
		buf.writeVarInt(this.anchorId);
		SerializeUtil.writeVector3d(buf, this.pos);
		SerializeUtil.writeQuaternionf(buf, this.rotation);
	}

	@Override
	public void handle(final ClientNetworkContext context) {
		context.relocateNode(this.id, this.anchorId, this.pos, this.rotation);
	}

	@Override
	public MessageType<RelocateNodeClientMessage> type() {
		return NetworkMessages.RELOCATE_NODE;
	}
}
