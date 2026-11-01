package com.github.litermc.vsfreerotation.util;

import net.minecraft.util.Mth;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

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
}
