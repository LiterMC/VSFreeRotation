// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package com.github.litermc.vsfreerotation.network.client;

import org.joml.Quaternionfc;
import org.joml.Vector3dc;

/**
 * The context under which clientbound packets are evaluated.
 */
public interface ClientNetworkContext {
	void moveNode(int id, Vector3dc movement, Quaternionfc rotate);
	void relocateNode(int id, int anchorId, Vector3dc pos, Quaternionfc rotation);
}
