package com.github.litermc.vsfreerotation.compat;

import com.github.litermc.vsfreerotation.platform.PlatformHelper;

public enum CompatMods {
	COMPUTERCRAFT("computercraft"),
	JADE("jade");

	private final String modId;

	private CompatMods(final String modId) {
		this.modId = modId;
	}

	public String getId() {
		return this.modId;
	}

	public boolean isLoaded() {
		return PlatformHelper.get().isModLoaded(this.getId());
	}
}
