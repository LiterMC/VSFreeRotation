package com.github.litermc.vsfreerotation.impl;

import com.github.litermc.vsfreerotation.accessor.LivingEntityAccessor;
import com.github.litermc.vsfreerotation.core.Node;
import com.github.litermc.vsfreerotation.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4dc;
import org.joml.Quaternionf;
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
	}

	/**
	 * Get the head rotation relative to the world
	 * @return absolute head rotation
	 */
	public Quaternionf getHeadRotation() {
		return this.getAbsRotation().rotateY(this.headYaw).rotateX(this.headPitch);
	}

	public Quaternionf getLastHeadRotation() {
		return this.getLastAbsRotation().rotateY(this.headYawO).rotateX(this.headPitchO);
	}

	/**
	 * Get the head rotation relative to the world
	 * @param alpha The ratio of current tick's rotation to previous one
	 * @return relative rotation
	 */
	public Quaternionf getHeadRotation(final float alpha) {
		return this.getLastHeadRotation().slerp(this.getHeadRotation(), alpha);
	}

	@Override
	protected void onUpdateAnchor(final Node oldAnchor, final Node newAnchor, final Matrix4dc transform) {
		super.onUpdateAnchor(oldAnchor, newAnchor, transform);
		final Vector3f lookVec = transform.transformDirection(new Vector3f(0, 0, 1).rotateY(this.headYaw).rotateX(this.headPitch));
		this.headPitch = (float) Math.asin(lookVec.y);
		if (Math.abs(this.headPitch) < Mth.HALF_PI - 0.001f) {
			this.headYaw = (float) Mth.atan2(lookVec.z, lookVec.x);
		}
	}

	@Override
	public void setOldPosAndRot() {
		super.setOldPosAndRot();
		this.headPitchO = this.getHeadPitch();
		this.headYawO = this.getHeadYaw();
	}

	@Override
	public void tick() {
		{
			final Vector3f angles = this.getLastHeadRotation().getEulerAnglesYXZ(new Vector3f());
			this.getEntity().xRotO = angles.x * Mth.RAD_TO_DEG;
			this.getEntity().yHeadRotO = this.getEntity().yRotO = -angles.y * Mth.RAD_TO_DEG;
		}
		super.tick();
		{
			final Vector3f angles = this.getHeadRotation().getEulerAnglesYXZ(new Vector3f());
			this.getEntity().setXRot(angles.x * Mth.RAD_TO_DEG);
			final float yRot = -angles.y * Mth.RAD_TO_DEG;
			this.getEntity().setYRot(yRot);
			this.getEntity().yHeadRot = yRot;
		}
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
