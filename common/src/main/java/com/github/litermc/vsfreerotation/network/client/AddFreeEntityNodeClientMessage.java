package com.github.litermc.vsfreerotation.network.client;

import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.network.MessageType;
import com.github.litermc.vsfreerotation.network.NetworkMessage;
import com.github.litermc.vsfreerotation.network.NetworkMessages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public final class AddFreeEntityNodeClientMessage implements NetworkMessage<ClientNetworkContext> {
	private final int id;
	private final int entityId;
	private final CompoundTag data;

	public AddFreeEntityNodeClientMessage(final FreeEntity<?> fe, final int entityId) {
		this.id = fe.getId();
		this.entityId = entityId;
		this.data = new CompoundTag();
		fe.save(this.data);
	}

	public AddFreeEntityNodeClientMessage(final FriendlyByteBuf buf) {
		this.id = buf.readVarInt();
		this.entityId = buf.readVarInt();
		this.data = buf.readNbt();
	}

	@Override
	public void write(final FriendlyByteBuf buf) {
		buf.writeVarInt(this.id);
		buf.writeVarInt(this.entityId);
		buf.writeNbt(this.data);
	}

	@Override
	public void handle(final ClientNetworkContext context) {
		context.addFreeEntity(this.id, this.entityId, this.data);
	}

	@Override
	public MessageType<AddFreeEntityNodeClientMessage> type() {
		return NetworkMessages.ADD_FREE_ENTITY;
	}
}
