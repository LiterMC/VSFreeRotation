package com.github.litermc.vsfreerotation.client;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Mod.EventBusSubscriber
public final class ClientEvents {
	private static final Vector3f CAMERA_ROT_VEC = new Vector3f();

	@SubscribeEvent
	public static void onComputeCamera(final ViewportEvent.ComputeCameraAngles event) {
		final Camera camera = event.getCamera();
		FreeEntity<?> fe = ((EntityAccessor) camera.getEntity()).vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		final Quaternionf rotation = camera.rotation();
		rotation.getEulerAnglesYXZ(CAMERA_ROT_VEC);
		event.setPitch(CAMERA_ROT_VEC.x * Mth.RAD_TO_DEG);
		event.setYaw(-CAMERA_ROT_VEC.y * Mth.RAD_TO_DEG);
		event.setRoll(CAMERA_ROT_VEC.z * Mth.RAD_TO_DEG);
	}
}
