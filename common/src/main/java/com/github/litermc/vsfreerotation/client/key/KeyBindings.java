package com.github.litermc.vsfreerotation.client.key;

import com.github.litermc.vsfreerotation.Constants;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
	public static final KeyMapping ROLL_CLOCKWISE = new KeyMapping(
		"key." + Constants.MOD_ID + ".roll_clockwise",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_C,
		"key.categories.movement"
	);

	public static final KeyMapping ROLL_COUNTER_CLOCKWISE = new KeyMapping(
		"key." + Constants.MOD_ID + ".roll_counter_clockwise",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_Z,
		"key.categories.movement"
	);

	public static final DoubleClickKeyMapping UNLOCK_HEAD_ROTATION = new DoubleClickKeyMapping(
		"key." + Constants.MOD_ID + ".unlock_head_rotation",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_LEFT_ALT,
		"key.categories.movement"
	);
}
