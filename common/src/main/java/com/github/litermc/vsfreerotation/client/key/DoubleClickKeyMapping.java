package com.github.litermc.vsfreerotation.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class DoubleClickKeyMapping extends KeyMapping {
	private long lastDown = 0;
	private long doubleClicked = 0;

	public DoubleClickKeyMapping(final String name, final InputConstants.Type type, final int defaultKey, final String category) {
		super(name, type, defaultKey, category);
	}

	public boolean consumeDoubleClick() {
		if (this.doubleClicked == 0) {
			return false;
		}
		final boolean valid = this.doubleClicked + 500 >= System.currentTimeMillis();
		this.doubleClicked = 0;
		return valid;
	}

	@Override
	public void setDown(final boolean value) {
		super.setDown(value);
		final long now = System.currentTimeMillis();
		if (!value) {
			if (this.lastDown != 0 && this.lastDown + 180 < now) {
				this.lastDown = 0;
			}
			return;
		}
		if (this.lastDown + 300 < now) {
			this.lastDown = now;
			return;
		}
		this.lastDown = 0;
		this.doubleClicked = now;
	}
}
