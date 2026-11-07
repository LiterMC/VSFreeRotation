package com.github.litermc.vsfreerotation.util;

import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

public final class MathUtil {
	public static final Quaterniondc ZERO_QUATD = new Quaterniond();
	public static final Quaternionfc ZERO_QUATF = new Quaternionf();
	public static final Vector3dc ZERO_VEC3D = new Vector3d();

	private MathUtil() {}

	/**
	 * @return normalized angle in range of (-{@link Math.PI}, {@link Math.PI}]
	 */
	public static final float normalizeAngle(float angle) {
		angle = (angle % Mth.TWO_PI + Mth.TWO_PI) % Mth.TWO_PI;
		return angle > Mth.PI ? angle - Mth.TWO_PI : angle;
	}

	public static final float lerpAngle(final float alpha, final float a, final float b) {
		return normalizeAngle(a + normalizeAngle(b - a) * alpha);
	}

	public static Vector2f getPitchYaw(final Quaternionfc rotation) {
		final Matrix3f mat = new Matrix3f().rotation(rotation);
		final Vector3f right = mat.getColumn(0, new Vector3f());
		final Vector3f up = mat.getColumn(1, new Vector3f());
		final Vector3f forward = mat.getColumn(2, new Vector3f());

		final Vector2f angles = new Vector2f();
		angles.x = (float) -Math.asin(Math.min(Math.max(forward.y, -1), 1));
		if (Math.abs(forward.y) < 0.999) {
			angles.y = (float) Mth.atan2(-forward.x, forward.z);
		} else {
			angles.y = (float) Mth.atan2(right.z, right.x);
			if (up.y < 0) {
				angles.y = MathUtil.normalizeAngle(angles.y + Mth.PI);
			}
		}
		return angles;
	}

	public static void getPitchYawRoll(final Quaternionfc rotation, final Vector3f dest) {
		final Matrix3f mat = new Matrix3f().rotation(rotation);
		final Vector3f right = mat.getColumn(0, new Vector3f());
		final Vector3f up = mat.getColumn(1, new Vector3f());
		final Vector3f forward = mat.getColumn(2, new Vector3f());

		dest.x = (float) -Math.asin(Math.min(Math.max(forward.y, -1), 1));
		if (Math.abs(forward.y) < 0.999) {
			dest.y = (float) Mth.atan2(-forward.x, forward.z);
			dest.z = (float) Mth.atan2(right.y, up.y);
		} else {
			dest.y = (float) Mth.atan2(right.z, right.x);
			if (up.y < 0) {
				dest.y = MathUtil.normalizeAngle(dest.y + Mth.PI);
			}
		}
	}
}
