package com.snipermod.forgeclient;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemBow;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Stop;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class MEventHandler {
	
	private final ArrowDirector instance = ArrowDirector.mod;
	EntityArmorStand view = null;
	//CameraController controller = null;
	double savedX = 0;
	double savedY = 0;
	double savedZ = 0;
	int firingMode = 1; //1 for movement 2 for sniper mode
	
	@SubscribeEvent
	public void onKeypress(KeyInputEvent event) {
		if(instance.key.isPressed()) {
			if(ArrowDirector.hasBowEquipped() == false) return;
			if(!instance.overhead) {
				if(view == null) {
					view = new EntityArmorStand(Minecraft.getMinecraft().player.world);
					/*if(controller == null) {
						controller = new CameraController(view);
					}*/
				}
				view.world = Minecraft.getMinecraft().player.world;
				if(instance.hasSwitched) {
					view.setLocationAndAngles(savedX, savedY, savedZ, -Minecraft.getMinecraft().player.rotationYawHead, 90.0f);
				} else {
					view.setLocationAndAngles(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY + 10.0D, Minecraft.getMinecraft().player.posZ, -Minecraft.getMinecraft().player.rotationYawHead, 90.0f);
				}
				Minecraft.getMinecraft().setRenderViewEntity(view);
			} else if(instance.overhead) {
				savedX = view.posX;
				savedY = view.posY;
				savedZ = view.posZ;
				Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player);
			}
			instance.overhead = !instance.overhead;
			if(Minecraft.getMinecraft().mouseHelper instanceof ForgeMouseWrapper) {
				ForgeMouseWrapper forgeMouse = (ForgeMouseWrapper) Minecraft.getMinecraft().mouseHelper;
				forgeMouse.isPlayerRotationDisabled = instance.overhead;
			}
			instance.hasSwitched = true;
		}
		if(instance.speed.isPressed()) {
			if(firingMode == 1) {
				firingMode = 2;
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Set firing mode to SNIPER"));
			} else if (firingMode == 2) {
				firingMode = 3;
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Set firing mode to FINE"));
			} else if(firingMode == 3) {
				firingMode = 1;
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Set firing mode to MOVEMENT"));
			}
		}
		if(instance.reset.isPressed()) {
			if(instance.overhead) {
				Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player);
				view.setPosition(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY + 10.0D, Minecraft.getMinecraft().player.posZ);
				Minecraft.getMinecraft().setRenderViewEntity(view);
			}
		}
		if(instance.trajectory.isPressed()) {
			instance.trajectoryIsFlat = !instance.trajectoryIsFlat;
			this.notifyTrajectory(instance.trajectoryIsFlat);
		}
	}
	
	void notifyTrajectory(boolean flat) {
		if(flat) {
			Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Set trajectory to flatter arc"));
			Minecraft.getMinecraft().player.sendMessage(new TextComponentString("WARNING: A flatter trajectory is more accurate and travels faster, but cannot reach over obstacles!"));
		} else {
			Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Set trajectory to higher arc"));
			Minecraft.getMinecraft().player.sendMessage(new TextComponentString("WARNING: A ballistic trajectory can reach hard to access areas, but at the expense of accuracy and a horrendous travel time!"));
		}
	}
	
	/*
	@SubscribeEvent
	public void onStop(Stop event) {
		if(event.getItem().getItem() instanceof ItemBow) {
			Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player);
		}
	}*/
		
	@SubscribeEvent
	public void onView(CameraSetup event) {
		if(event.getEntity() == view) {
			event.setPitch(view.rotationPitch);
			event.setYaw(view.rotationYaw);
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(instance.overhead) {
			instance.update(event);
		}
	}
	
}
