package com.github.litermc.vsfreerotation.core;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public final class NodeManager {
	private NodeManager() {}

	private static final AtomicInteger ID_ALLOCATOR = new AtomicInteger();
	private static final Set<NodeProvider> PROVIDERS = new CopyOnWriteArraySet<>();

	/**
	 * Register a node provider. The nodes provided by the provider can be returned via {@link getNode}.
	 * Multiple node providers must not have different (ID, Node) pairs, or unexpected result may happen to the program.
	 * Thread safe operation.
	 * @param provider The node provider to be added.
	 */
	public static void registerProvider(final NodeProvider provider) {
		PROVIDERS.add(provider);
	}

	/**
	 * Unregister a node provider. {@link getNode} will no longer invoke the provider after.
	 * Thread safe operation.
	 * @param provider The node provider to be removed.
	 */
	public static void unregisterProvider(final NodeProvider provider) {
		PROVIDERS.remove(provider);
	}

	public static int allocateId() {
		final int id = ID_ALLOCATOR.incrementAndGet();
		if (id == 0) {
			// Techinally Minecraft's entity IDs will run out / duplicated before this, but just in case.
			throw new RuntimeException("Too many IDs are allocated in the life cycle");
		}
		return id;
	}

	/**
	 * Get a node from node providers registered by {@link registerProvider}.
	 * Thread safe operation.
	 * @param id The node's ID.
	 * @return The node, or {@code null} if not exists.
	 */
	public static Node getNode(final int id) {
		if (id == 0) {
			return null;
		}
		for (final NodeProvider provider : PROVIDERS) {
			final Node node = provider.getNode(id);
			if (node != null) {
				return node;
			}
		}
		return null;
	}
}
