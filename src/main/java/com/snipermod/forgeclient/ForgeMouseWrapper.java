package com.snipermod.forgeclient;

import org.lwjgl.input.Mouse;

import net.minecraft.util.MouseHelper;

public class ForgeMouseWrapper extends MouseHelper {
	
	boolean isPlayerRotationDisabled = false;
	int secretMouseX;
	int secretMouseY;
	
	@Override
	public void mouseXYChange() {
		if(!isPlayerRotationDisabled) {
			super.mouseXYChange();
		} else {
			this.secretMouseX = Mouse.getDX();
			this.secretMouseY = Mouse.getDY();
		}
	}
	
}
