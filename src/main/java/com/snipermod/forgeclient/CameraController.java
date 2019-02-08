package com.snipermod.forgeclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;

public class CameraController {
	
	Minecraft minecraft = Minecraft.getMinecraft();
	Entity entity;
	
	public CameraController(Entity entity) {
		this.entity = entity;
	}
	
	public void update() {
		if(this.entity == minecraft.getRenderViewEntity()) {
			if(minecraft.mouseHelper instanceof ForgeMouseWrapper) {
				ForgeMouseWrapper mouse = (ForgeMouseWrapper) Minecraft.getMinecraft().mouseHelper;
				if(!mouse.isPlayerRotationDisabled) {
					System.out.println("Cannot control active player");
					return;
				}
				KeyBinding w = minecraft.gameSettings.keyBindForward;
				KeyBinding a = minecraft.gameSettings.keyBindLeft;
				KeyBinding s = minecraft.gameSettings.keyBindBack;
				KeyBinding d = minecraft.gameSettings.keyBindRight;
				KeyBinding space = minecraft.gameSettings.keyBindJump;
				KeyBinding shift = minecraft.gameSettings.keyBindSneak;
				
				int front = 0;
				int side = 0;
				int mouseX = mouse.secretMouseX;
				int mouseY = mouse.secretMouseY;
				int vertical = 0;
				if(w.isKeyDown()) front += 1;
				if(s.isKeyDown()) front -= 1;
				if(a.isKeyDown()) side -= 1;
				if(d.isKeyDown()) side += 1;
				if(space.isKeyDown()) vertical += 0.5;
				if(shift.isKeyDown()) vertical -= 0.5;
				
				minecraft.setRenderViewEntity(minecraft.player);
				
				float sensitivity = minecraft.gameSettings.mouseSensitivity * 0.6F + 0.2F;
				sensitivity = sensitivity * sensitivity * sensitivity * 8.0F;
				mouseX *= sensitivity;
				mouseY *= sensitivity;
				entity.turn(mouseX, mouseY);
				
				double moveX = -(Math.sin(Math.toRadians(entity.rotationYaw)) * front);
				double moveZ = (Math.cos(Math.toRadians(entity.rotationYaw)) * front);
				moveX += -(Math.sin(Math.toRadians(entity.rotationYaw + 90)) * side);
				moveZ += (Math.cos(Math.toRadians(entity.rotationYaw + 90)) * side);
				
				moveX += entity.posX;
				moveZ += entity.posZ;
				vertical += entity.posY;
				
				entity.setPosition(moveX, vertical, moveZ);
				
				minecraft.setRenderViewEntity(this.entity);
			}
		}
	}
	
}
