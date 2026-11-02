// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package com.github.litermc.vsfreerotation.client.platform;

import com.github.litermc.vsfreerotation.network.NetworkMessage;
import com.github.litermc.vsfreerotation.network.server.ServerNetworkContext;
import com.github.litermc.vsfreerotation.platform.Services;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.jetbrains.annotations.Nullable;

public interface ClientPlatformHelper {
	static ClientPlatformHelper get() {
		var instance = Instance.INSTANCE;
		return instance == null ? Services.raise(ClientPlatformHelper.class, Instance.ERROR) : instance;
	}

	final class Instance {
		static final @Nullable ClientPlatformHelper INSTANCE;
		static final @Nullable Throwable ERROR;

		static {
			var helper = Services.tryLoad(ClientPlatformHelper.class);
			INSTANCE = helper.instance();
			ERROR = helper.error();
		}

		private Instance() {
		}
	}

	/**
	 * Convert a serverbound {@link NetworkMessage} to a Minecraft {@link Packet}.
	 *
	 * @param message The messsge to convert.
	 * @return The converted message.
	 */
	Packet<ServerGamePacketListener> createPacket(NetworkMessage<ServerNetworkContext> message);
}
