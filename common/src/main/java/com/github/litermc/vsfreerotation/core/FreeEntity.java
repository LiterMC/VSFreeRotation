package com.github.litermc.vsfreerotation.core;

import com.github.litermc.vsfreerotation.util.MathUtil;
import com.github.litermc.vsfreerotation.util.SerializeUtil;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;

public class FreeEntity {
	private boolean noPrevTick = true;
	private Vector3dc pos = MathUtil.ZERO_VEC3D;
	private Vector3dc posO = MathUtil.ZERO_VEC3D;
	private Vector3dc posL = MathUtil.ZERO_VEC3D;
	private Vector3dc posLO = MathUtil.ZERO_VEC3D;
	private Quaternionfc rotation = MathUtil.ZERO_QUATF;
	private Quaternionfc rotationO = MathUtil.ZERO_QUATF;
	private Quaternionfc rotationL = MathUtil.ZERO_QUATF;
	private Quaternionfc rotationLO = MathUtil.ZERO_QUATF;
	private BlockPos origin = BlockPos.ZERO;
	private BlockPos originL = BlockPos.ZERO;
	private EntityDimensions dimensions;

	private Vector3dc movement = MathUtil.ZERO_VEC3D;

	public FreeEntity(final EntityDimensions dimensions) {
		this.dimensions = dimensions;
	}

	/**
	 * Get the position relative to origin
	 * @return relative position
	 * @see setPosition
	 * @see getOrigin
	 */
	public Vector3dc getPosition() {
		return this.pos;
	}

	/**
	 * Set the position relative to origin
	 * @param pos relative position, must be immutable
	 * @see getPosition
	 * @see getOrigin
	 */
	public void setPosition(final Vector3dc pos) {
		this.pos = pos;
	}

	/**
	 * Get the position relative to previous tick's origin
	 * @param alpha The ratio of current tick's position to previous one
	 * @return relative position
	 */
	public Vector3d getPositionL(final double alpha) {
		return this.posLO.lerp(this.posL, alpha, new Vector3d());
	}

	/**
	 * Get the transformed position (relative to world origin)
	 * @return the absolute position
	 */
	public Vector3dc getAbsPosition() {
		return this.getTransform().transformPosition(this.getPosition());
	}

	/**
	 * Get the transformed position (relative to world origin)
	 * @param alpha The ratio of current tick's position to previous one
	 * @return the absolute position
	 */
	public Vector3d getAbsPosition(final double alpha) {
		return this.getTransformL(alpha).transformPosition(this.getPositionL(alpha));
	}

	/**
	 * Get the rotation relative to origin
	 * @return relative rotation
	 * @see setRotation
	 * @see getOrigin
	 */
	public Quaternionfc getRotation() {
		return this.rotation;
	}

	/**
	 * Set the rotation relative to origin
	 * @param rotation relative rotation, must be immutable
	 * @see getRotation
	 * @see getOrigin
	 */
	public void setRotation(final Quaternionfc rotation) {
		this.rotation = rotation;
	}

	/**
	 * Get the rotation relative to previous tick's origin
	 * @param alpha The ratio of current tick's rotation to previous one
	 * @return relative rotation
	 */
	public Quaternionfc getRotationL(final double alpha) {
		return this.rotationLO.slerp(this.rotationL, alpha, new Quaternionfc());
	}

	/**
	 * Get the transformed rotation (relative to world origin)
	 * @return the absolute rotation
	 */
	public Quaternionfc getAbsRotation() {
		return this.getTransform().getNormalizedRotation(new Quaternionf()).mul(this.getRotation());
	}

	/**
	 * Get the transformed rotation (relative to world origin)
	 * @param alpha The ratio of current tick's position to previous one
	 * @return the absolute rotation
	 */
	public Quaternionfc getAbsRotation(final double alpha) {
		return this.getTransformL(alpha).getNormalizedRotation(new Quaternionf()).mul(this.getRotationL(alpha));
	}

	/**
	 * Get the origin block
	 * @return the origin's block position
	 */
	public BlockPos getOrigin() {
		return this.origin;
	}

	/**
	 * Set the origin but do NOT update relative position.
	 * @param origin the origin's block position
	 */
	public void setOrigin(final BlockPos origin) {
		this.origin = origin;
	}

	/**
	 * Get transform matrix from relative position and rotation to world's.
	 * @return the transform matrix
	 */
	public Matrix4dc getTransform() {
		final BlockPos origin = this.origin;
		final Ship ship = VSGameUtilsKt.getShipManagingPos(this.getLevel(), origin);
		if (ship == null) {
			return MathUtil.ZERO_MATRIX4D;
		}
		return ship.getTransform().getShipToWorld().translate(origin.getX(), origin.getY(), origin.getZ(), new Matrix4d());
	}

	/**
	 * Get transform matrix fromrelative position and rotation to world's for previous tick's origin.
	 * @param alpha the ratio of current tick's transform to the previous tick
	 * @return the transform matrix
	 */
	public Matrix4dc getTransformL(final double alpha) {
		final BlockPos origin = this.originL;
		final Ship ship = VSGameUtilsKt.getShipManagingPos(this.getLevel(), origin);
		if (ship == null) {
			return MathUtil.ZERO_MATRIX4D;
		}
		final Matrix4d transform = new Matrix4d(ship.getPrevTickTransform().getShipToWorld());
		if (alpha != 0) {
			transform.lerp(ship.getTransform().getShipToWorld(), alpha);
		}
		return transform.translate(origin.getX(), origin.getY(), origin.getZ());
	}

	/**
	 * Set the origin and update relative position & rotation.
	 * @param origin the origin position
	 */
	public void updateOrigin(final BlockPos origin) {
		final Matrix4dc oldTransform = this.getTransform();
		this.setOrigin(origin);
		final Matrix4dc newTransform = this.getTransform();
		final Matrix4dc transform = newTransform.invert(new Matrix4d()).mul(oldTransform);
		this.setPosition(transform.transformPosition(this.getPosition(), new Vector3d()));
		this.setRotation(transform.getNormalizedRotation(new Quaternionf()).mul(this.getRotation(), new Vector3d()));
		this.setMovement(transform.transformDirection(this.getMovement(), new Vector3d()));
	}

	/**
	 * Get the movement relative to origin
	 * @return relative movement
	 */
	public Vector3dc getMovement() {
		return this.movement;
	}

	/**
	 * Set the movement relative to origin
	 * @param movement relative movement
	 */
	public void setMovement(final Vector3dc movement) {
		this.movement = movement;
	}

	/**
	 * Add the movement
	 * @param movement movement delta
	 */
	public final void addMovement(final Vector3dc movement) {
		this.setMovement(this.getMovement().add(movement, new Vector3d()));
	}

	public void save(final CompoundTag data) {
		SerializeUtil.putVector3d(data, this.pos);
		data.put("rotation", SerializeUtil.quaternionfToList(this.rotation));
		data.putIntArray("origin", SerializeUtil.blockPosToArray(this.origin));
	}

	public void load(final CompoundTag data) {
		SerializeUtil.getVector3d(data, this.pos);
		this.posLO = this.posL = this.posO = this.pos;
		SerializeUtil.listToQuaternionf(data.getList("rotation"), this.rotation);
		this.rotationLO = this.rotationL = this.rotationO = this.rotation;
		this.originL = this.origin = SerializeUtil.arrayToBlockPos(data.getIntArray("origin"));
	}
}
