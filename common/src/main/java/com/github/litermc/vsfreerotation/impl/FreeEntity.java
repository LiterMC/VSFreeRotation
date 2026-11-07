package com.github.litermc.vsfreerotation.impl;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.core.Node;
import com.github.litermc.vsfreerotation.core.NodeManager;
import com.github.litermc.vsfreerotation.util.MathUtil;
import com.github.litermc.vsfreerotation.util.SerializeUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4dc;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.HashMap;
import java.util.Map;

public class FreeEntity<T extends Entity> extends Node {
	private static final Map<Class<?>, ClientFactory<?>> CLIENT_FACTORIES = new HashMap<>();
	private static final Map<Class<?>, ServerFactory<?>> SERVER_FACTORIES = new HashMap<>();

	static {
		registerFactory(Entity.class, FreeEntity::new, FreeEntity::new);
		registerFactory(LivingEntity.class, FreeLivingEntity::new, FreeLivingEntity::new);
	}

	@NotNull
	private final T entity;
	private int anchorId = 0;

	private Vector3dc movement = MathUtil.ZERO_VEC3D;

	protected FreeEntity(final @NotNull T entity) {
		super();
		this.entity = entity;
	}

	protected FreeEntity(final int id, final @NotNull T entity) {
		super(id);
		this.entity = entity;
	}

	@NotNull
	public final T getEntity() {
		return this.entity;
	}

	public final SynchedEntityData getEntityData() {
		return this.entity.getEntityData();
	}

	public boolean isFreeRotationEnabled() {
		return ((EntityAccessor) this.entity).vsfr$isFreeRotationEnabled();
	}

	public void setFreeRotationFlag(final boolean flag) {
		((EntityAccessor) this.entity).vsfr$setFreeRotationFlag(flag);
	}

	public boolean isBodyRotationLocked() {
		return ((EntityAccessor) this.entity).vsfr$isBodyRotationLocked();
	}

	public void setBodyRotationLock(final boolean lock) {
		((EntityAccessor) this.entity).vsfr$setBodyRotationLock(lock);
	}

	public boolean canFreeRotate() {
		return this.isFreeRotationEnabled() && !this.entity.isPassenger();
	}

	@Override
	public void setPosition(final Vector3dc pos) {
		super.setPosition(pos);
		final Vector3d absPos = new Vector3d(this.getPosition());
		absPos.y -= this.entity.getBbHeight() / 2;
		this.getAnchorTransform().transformPosition(absPos);
		((EntityAccessor) this.entity).vsfr$raw$setPosRaw(absPos.x, absPos.y, absPos.z);
	}

	@Override
	public void setAnchor(final Node anchor) {
		super.setAnchor(anchor);
		this.anchorId = anchor == null ? 0 : anchor.getId();
	}

	/**
	 * Get the hitbox rotation relative to the world
	 * @return absolute hitbox rotation
	 */
	public Quaternionf getHitboxRotation() {
		return this.getAbsRotation();
	}

	public Quaternionf getLastHitboxRotation() {
		return this.getLastAbsRotation();
	}

	/**
	 * Get the hitbox rotation relative to the world
	 * @param alpha The ratio of current tick's rotation to previous one
	 * @return relative rotation
	 */
	public Quaternionf getHitboxRotation(final float alpha) {
		return this.getAbsRotation(alpha);
	}

	/**
	 * Get the movement relative to anchor in blocks/tick
	 * @return relative movement
	 */
	public Vector3dc getMovement() {
		return this.movement;
	}

	/**
	 * Set the movement relative to anchor in blocks/tick
	 * @param movement relative movement
	 */
	public void setMovement(final Vector3dc movement) {
		this.movement = movement;
		final Vector3d absMovement = this.getAnchorTransform().transformDirection(movement, new Vector3d());
		((EntityAccessor) this.entity).vsfr$raw$setDeltaMovement(new Vec3(absMovement.x, absMovement.y, absMovement.z));
	}

	/**
	 * Add the movement
	 * @param movement movement delta
	 */
	public final void addMovement(final Vector3dc movement) {
		this.setMovement(this.getMovement().add(movement, new Vector3d()));
	}

	/**
	 * Get the dimensions of the entity.
	 */
	public AABBdc getDimensions() {
		final double width_2 = this.entity.getBbWidth() / 2;
		final double height_2 = this.entity.getBbHeight() / 2;
		return new AABBd(-width_2, -height_2, -width_2, width_2, height_2, width_2);
	}

	@Override
	protected void onUpdateAnchor(final Node oldAnchor, final Node newAnchor, final Matrix4dc transform) {
		super.onUpdateAnchor(oldAnchor, newAnchor, transform);
		this.setMovement(transform.transformDirection(this.getMovement(), new Vector3d()));
	}

	public void maybeBackOffFromEdge(final Vector3d movement, final MoverType moverType) {
		// throw new UnsupportedOperationException("Unimplemented method 'maybeBackOffFromEdge'");
	}

	public void setOldPosAndRot() {
		this.posO = this.getPosition();
		this.rotationO = this.getRotation();
		this.anchorO = this.getAnchor();
	}

	/**
	 * Will be invoked at the beginning of {@link Entity#baseTick()}
	 */
	public void tick() {
		if (this.anchorId != 0 && this.getAnchor() == null) {
			final Node anchor = NodeManager.getNode(this.anchorId);
			if (anchor != null) {
				this.setAnchor(anchor);
			}
		}
		this.setOldPosAndRot();
	}

	public void save(final CompoundTag data) {
		SerializeUtil.putVector3d(data, this.getPosition());
		data.put("rotation", SerializeUtil.quaternionfToList(this.getRotation()));
		final Node anchor = this.getAnchor();
		if (anchor != null) {
			data.putInt("anchor", anchor.getId());
		} else if (this.anchorId != 0) {
			data.putInt("anchor", this.anchorId);
		}
	}

	public void load(final CompoundTag data) {
		this.setPosition(SerializeUtil.getVector3d(data));
		this.posO = this.getPosition();
		this.setRotation(SerializeUtil.listToQuaternionf(data.getList("rotation", Tag.TAG_FLOAT)));
		this.rotationO = this.getRotation();
		this.anchorId = data.getInt("anchor");
		this.setAnchor(NodeManager.getNode(this.anchorId));
		this.anchorO = this.getAnchor();
	}

	public static final <T extends Entity> void registerFactory(Class<T> entityClass, ClientFactory<T> client, ServerFactory<T> server) {
		CLIENT_FACTORIES.put(entityClass, client);
		SERVER_FACTORIES.put(entityClass, server);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final FreeEntity<?> createClient(final int id, final @NotNull Entity entity) {
		for (Class<?> clazz = entity.getClass(); Entity.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
			final ClientFactory factory = CLIENT_FACTORIES.get(clazz);
			if (factory != null) {
				return factory.create(id, entity);
			}
		}
		throw new IllegalStateException("Base factory missing in factories map!");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final FreeEntity<?> createServer(final @NotNull Entity entity) {
		for (Class<?> clazz = entity.getClass(); Entity.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
			final ServerFactory factory = SERVER_FACTORIES.get(clazz);
			if (factory != null) {
				return factory.create(entity);
			}
		}
		throw new IllegalStateException("Base factory missing in factories map!");
	}

	@FunctionalInterface
	public interface ClientFactory<T extends Entity> {
		FreeEntity<? extends T> create(int id, T entity);
	}

	@FunctionalInterface
	public interface ServerFactory<T extends Entity> {
		FreeEntity<? extends T> create(T entity);
	}
}
