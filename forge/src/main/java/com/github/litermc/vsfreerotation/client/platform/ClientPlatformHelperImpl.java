// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package com.github.litermc.vsfreerotation.client.platform;

import com.github.litermc.vsfreerotation.network.NetworkMessage;
import com.github.litermc.vsfreerotation.network.server.ServerNetworkContext;
import com.github.litermc.vsfreerotation.platform.NetworkHandler;
import com.google.auto.service.AutoService;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

@AutoService(ClientPlatformHelper.class)
public class ClientPlatformHelperImpl implements ClientPlatformHelper {
	@Override
	public Packet<ServerGamePacketListener> createPacket(NetworkMessage<ServerNetworkContext> message) {
		return NetworkHandler.createServerboundPacket(message);
	}
}
