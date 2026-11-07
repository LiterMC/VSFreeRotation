package com.github.litermc.vsfreerotation.impl;

import com.github.litermc.vsfreerotation.accessor.LivingEntityAccessor;
import com.github.litermc.vsfreerotation.core.Node;
import com.github.litermc.vsfreerotation.util.MathUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4dc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class FreeLivingEntity<T extends LivingEntity> extends FreeEntity<T> {
	private float headPitch = 0;
	private float headYaw = 0;

	public float headPitchO = 0;
	public float headYawO = 0;

	protected FreeLivingEntity(final @NotNull T entity) {
		super(entity);
		this.setFreeRotationFlag(true);
		this.setBodyRotationLock(true);
	}

	protected FreeLivingEntity(final int id, final @NotNull T entity) {
		super(id, entity);
	}

	public boolean isHeadRotationLocked() {
		return ((LivingEntityAccessor) this.getEntity()).vsfr$isHeadRotationLocked();
	}

	public void setHeadRotationLock(final boolean lock) {
		((LivingEntityAccessor) this.getEntity()).vsfr$setHeadRotationLock(lock);
	}

	/**
	 * Get the pitch of the head relative to {@link getRotation() rotation}
	 * @return pitch of head
	 */
	public float getHeadPitch() {
		return this.headPitch;
	}

	public float getLastHeadPitch() {
		return this.headPitchO;
	}

	/**
	 * Get the pitch of the head relative to {@link getRotation() rotation}
	 * @param alpha The ratio of current tick's rotation to previous one
	 * @return pitch of head
	 */
	public float getHeadPitch(final float alpha) {
		return MathUtil.lerpAngle(alpha, this.headPitchO, this.headPitch);
	}

	/**
	 * Set the head pitch relative to current rotation
	 * @param headPitch relative head pitch
	 */
	public void setHeadPitch(final float headPitch) {
		this.headPitch = headPitch;
		this.recalcHeadRotation();
	}

	/**
	 * Get the yaw of the head relative to {@link getRotation() rotation}
	 * @return yaw of head
	 */
	public float getHeadYaw() {
		return this.headYaw;
	}

	public float getLastHeadYaw() {
		return this.headYawO;
	}

	/**
	 * Get the yaw of the head relative to {@link getRotation() rotation}
	 * @param alpha The ratio of current tick's rotation to previous one
	 * @return yaw of head
	 */
	public float getHeadYaw(final float alpha) {
		return MathUtil.lerpAngle(alpha, this.headYawO, this.headYaw);
	}

	/**
	 * Set the head yaw relative to current rotation
	 * @param headYaw relative head yaw
	 */
	public void setHeadYaw(final float headYaw) {
		this.headYaw = headYaw;
		this.recalcHeadRotation();
	}

	/**
	 * Get the head rotation relative to the world
	 * @return absolute head rotation
	 */
	public Quaternionf getHeadRotation() {
		return this.getAbsRotation().rotateY(this.headYaw).rotateX(this.headPitch).normalize();
	}

	public Quaternionf getLastHeadRotation() {
		return this.getLastAbsRotation().rotateY(this.headYawO).rotateX(this.headPitchO).normalize();
	}

	/**
	 * Get the head rotation relative to the world
	 * @param alpha The ratio of current tick's rotation to previous one
	 * @return relative rotation
	 */
	public Quaternionf getHeadRotation(final float alpha) {
		return this.getLastHeadRotation().slerp(this.getHeadRotation(), alpha);
	}

	public void recalcHeadRotation() {
		final Quaternionfc rot = this.getHeadRotation();
		final Vector2f angles = MathUtil.getPitchYaw(rot);

		final LivingEntity entity = this.getEntity();
		entity.setXRot(angles.x * Mth.RAD_TO_DEG);
		final float yRot = angles.y * Mth.RAD_TO_DEG;
		entity.setYRot(yRot);
		entity.yHeadRot = yRot;

		if (Math.signum(yRot) != Math.signum(entity.yRotO)) {
			entity.yHeadRotO = entity.yRotO = MathUtil.normalizeAngle(entity.yRotO - yRot) + yRot;
		}
	}

	public void recalcLastHeadRotation() {
		final Quaternionfc rot = this.getLastHeadRotation();
		final Vector2f angles = MathUtil.getPitchYaw(rot);

		final LivingEntity entity = this.getEntity();
		entity.xRotO = angles.x * Mth.RAD_TO_DEG;
		entity.yHeadRotO = entity.yRotO = angles.y * Mth.RAD_TO_DEG;
	}

	@Override
	protected void onUpdateAnchor(final Node oldAnchor, final Node newAnchor, final Matrix4dc transform) {
		super.onUpdateAnchor(oldAnchor, newAnchor, transform);
		final Vector2f angles = MathUtil.getPitchYaw(new Quaternionf().rotationY(this.headYaw).rotateX(this.headPitch));
		this.headPitch = angles.x;
		this.headYaw = angles.y;
	}

	@Override
	public void setOldPosAndRot() {
		super.setOldPosAndRot();
		this.headPitchO = this.getHeadPitch();
		this.headYawO = this.getHeadYaw();
		this.recalcLastHeadRotation();
	}

	@Override
	public void tick() {
		super.tick();
		this.recalcHeadRotation();
	}

	@Override
	public void save(final CompoundTag data) {
		super.save(data);
		data.putFloat("headYaw", this.headYaw);
	}

	@Override
	public void load(final CompoundTag data) {
		super.load(data);
		this.setHeadPitch(0);
		this.headPitchO = this.getHeadPitch();
		this.setHeadYaw(data.getFloat("headYaw"));
		this.headYawO = this.getHeadYaw();
	}
}
