package com.snipermod.forgeclient;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(clientSideOnly = true, name = ArrowDirector.MODNAME, modid = ArrowDirector.MODID, version = ArrowDirector.VERSION, acceptedMinecraftVersions = ArrowDirector.ACCEPTED_MINECRAFT_VERSIONS)
public class ArrowDirector {
	
    public static final String MODID = "forgeclient";
    public static final String VERSION = "1.0";
    public static final String MODNAME = "snipermod";
    public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.11.2]";
    boolean overhead = false;
    boolean hasSwitched = false;
    ClientTickEvent updaterEvent = null;
    
    double zoom = 1.0D;
    
    @Instance
    public static ArrowDirector mod;
    MEventHandler modListener;
    boolean trajectoryIsFlat = false;
    
    public KeyBinding reset = new KeyBinding("Reset View Location", Keyboard.KEY_J, "key.categories.misc");
    public KeyBinding speed = new KeyBinding("Toggle Speed Scrolling", Keyboard.KEY_H, "key.categories.misc");
    public KeyBinding key = new KeyBinding("Toggle Viewpoint Key", Keyboard.KEY_G, "key.categories.misc");
    public KeyBinding trajectory = new KeyBinding("Toggle Trajectory", Keyboard.KEY_K, "key.categories.misc");
    
    //Note to self: Gravity for arrows is 20 blocks/second and drag is 0.99(Current velocity) which is called every tick 
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Initialising Forge Mod");
        ClientRegistry.registerKeyBinding(key);
        ClientRegistry.registerKeyBinding(speed);
        ClientRegistry.registerKeyBinding(reset);
        ClientRegistry.registerKeyBinding(trajectory);
        modListener = new MEventHandler();
        MinecraftForge.EVENT_BUS.register(modListener);
        Minecraft.getMinecraft().mouseHelper = new ForgeMouseWrapper();
    }
    
    public static boolean hasBowEquipped() {
    	if(Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu) return false;
    	if(Minecraft.getMinecraft().player == null) return false;
    	return Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ItemBow;
    }
    
    public void update(ClientTickEvent event) {
    	
    	updaterEvent = event;
    	
    	if(ArrowDirector.hasBowEquipped() == false) {
    		Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player);
    		this.overhead = false;
    		if(Minecraft.getMinecraft().mouseHelper instanceof ForgeMouseWrapper) {
        		ForgeMouseWrapper forgeMouse = (ForgeMouseWrapper) Minecraft.getMinecraft().mouseHelper;
        		forgeMouse.isPlayerRotationDisabled = false;
        	}
    		return;
    	}
    	
    	if(modListener.view == null) return;
    	if(Minecraft.getMinecraft().getRenderViewEntity() != modListener.view) {
    		Minecraft.getMinecraft().setRenderViewEntity(modListener.view);
    	}
    	EntityArmorStand view = modListener.view;
    	//modListener.controller.update();
    	
    	ForgeMouseWrapper forgeMouse = null;
    	if(Minecraft.getMinecraft().mouseHelper instanceof ForgeMouseWrapper) {
    		forgeMouse = (ForgeMouseWrapper) Minecraft.getMinecraft().mouseHelper;
    	}
    	
    	int mouseX = forgeMouse.secretMouseX;
    	int mouseY = forgeMouse.secretMouseY;
    	boolean xNegative = false;
    	boolean yNegative = false;
    	
    	if(!(mouseX == 0)) {
    		Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player);
    		
    		if(mouseX < 0) xNegative = true;
    		
    		double magnitude = 0.3;
    		
    		if(modListener.firingMode == 1) {
    			magnitude = 0.5;
    		}
    		
    		if(modListener.firingMode == 3) {
    			magnitude = 0.15;
    		}
    		
    		double moveX = -(Math.sin(Math.toRadians(view.rotationYaw + 90)) * magnitude);
    		double moveZ = (Math.cos(Math.toRadians(view.rotationYaw + 90)) * magnitude);
    		
    		if(xNegative) {
    			moveX = -(moveX);
    			moveZ = -(moveZ);
    		}
    		
    		moveX = -(moveX);
    		moveZ = -(moveZ);
    		
    		view.setLocationAndAngles(view.posX + moveX, view.posY, view.posZ + moveZ, view.rotationYaw, 90.0f);
    	}
    	
    	if(!(mouseY == 0)) {
    		Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player);
    		
    		if(mouseY < 0) yNegative = true;
    		
            double magnitude = 0.3;
    		
    		if(modListener.firingMode == 1) {
    			magnitude = 0.5;
    		}
    		
    		if(modListener.firingMode == 3) {
    			magnitude = 0.15;
    		}
    		
    		double moveX = -(Math.sin(Math.toRadians(view.rotationYaw)) * magnitude);
    		double moveZ = (Math.cos(Math.toRadians(view.rotationYaw)) * magnitude);
    		
    		if(yNegative) {
    			moveX = -(moveX);
    			moveZ = -(moveZ);
    		}
    		
    		moveX = -(moveX);
    		moveZ = -(moveZ);
    		
    		view.setLocationAndAngles(view.posX + moveX, view.posY, view.posZ + moveZ, view.rotationYaw, 90.0f);
    	}
    	
    	if(Minecraft.getMinecraft().getRenderViewEntity() != modListener.view) {
    		Minecraft.getMinecraft().setRenderViewEntity(modListener.view);
    	}
    	
    	RayTraceResult tracer = view.rayTrace(100.0D, 1.0f);
    	
    	double targetX = 0;
    	double targetY = 0;
    	double targetZ = 0;
    	
    	if(tracer.typeOfHit == Type.MISS) return;
    	
    	if(tracer.typeOfHit == Type.BLOCK) {
    		targetX = tracer.hitVec.xCoord;
    		targetY = tracer.hitVec.yCoord;
    		targetZ = tracer.hitVec.zCoord;
    	}
    	
    	if(tracer.typeOfHit == Type.ENTITY) {
    		targetX = tracer.entityHit.posX;
    		targetY = tracer.entityHit.posY;
    		targetZ = tracer.entityHit.posZ;
    	}
    	
    	Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player);
    	view.setLocationAndAngles(view.posX, targetY + (5.0D), view.posZ, view.rotationYaw, 90.0f);
    	Minecraft.getMinecraft().setRenderViewEntity(modListener.view);
    	
    	//int charge = 72000 - Minecraft.getMinecraft().player.getItemInUseCount();
    	float velocity = 53.0f;
    	System.out.println(velocity);
    	float gravity = 20f;
    	double range = Math.sqrt(((velocity * velocity * velocity * velocity) / 20 * 20));
    	double x = targetX - Minecraft.getMinecraft().player.posX;
    	double y = targetY - (Minecraft.getMinecraft().player.posY + Minecraft.getMinecraft().player.getEyeHeight());
		double z = targetZ - Minecraft.getMinecraft().player.posZ;
		double distance = Math.sqrt(x * x + z * z);
		
		Minecraft.getMinecraft().player.rotationYaw = (float) Math.toDegrees(Math.atan2(targetZ - Minecraft.getMinecraft().player.posZ, targetX - Minecraft.getMinecraft().player.posX)) - 90.0F;
		
		System.out.println("V Square: " + velocity * velocity);
		System.out.println("2yv Square" + 2 * y * velocity * velocity);
		System.out.println("gravity distance " + gravity * distance);
		System.out.println("Gravity Distance square " + gravity * distance * distance);
		System.out.println("Gravity Equation " + gravity * (gravity * distance * distance + 2 * y * velocity * velocity));
		System.out.println("Unroot " + ((velocity * velocity * velocity * velocity) - gravity * (gravity * distance * distance + 2 * y * velocity * velocity)));
		System.out.println("Root " + Math.sqrt( (velocity * velocity * velocity * velocity) - gravity * ( (gravity * (distance * distance)) + (2 * y * (velocity * velocity)) ) ) );
    	
		/*
    	float velocitySq = velocity * velocity;
		float velocityQuad = velocitySq * velocitySq;
		double distanceSq = distance * distance;
		double gravityDistance = distance * gravity;
		double gravityDistanceSq = gravity * distanceSq;
		double differential = 2 * y * velocitySq;
		double multiply = gravityDistanceSq + differential;
		double subtract = gravity * multiply;
		double square = velocityQuad - subtract;
		double sqrt = MathHelper.sqrt(square);
		*/
		
    	if(trajectoryIsFlat) {
    		Minecraft.getMinecraft().player.rotationPitch = (float) -Math.toDegrees(Math.atan(((velocity * velocity) - Math.sqrt((velocity * velocity * velocity * velocity) - gravity * ((gravity * (distance * distance)) + (2 * y * (velocity * velocity))))) / (gravity * distance)));
    	} else {
    		Minecraft.getMinecraft().player.rotationPitch = (float) -Math.toDegrees(Math.atan(((velocity * velocity) + Math.sqrt((velocity * velocity * velocity * velocity) - gravity * ((gravity * (distance * distance)) + (2 * y * (velocity * velocity))))) / (gravity * distance))) + 4;	
    	} 
    	
    	if(Minecraft.getMinecraft().player.rotationPitch != Minecraft.getMinecraft().player.prevRotationPitch || Minecraft.getMinecraft().player.rotationYaw != Minecraft.getMinecraft().player.prevRotationYaw) {
    		Minecraft.getMinecraft().player.connection.sendPacket(new CPacketPlayer.Rotation(Minecraft.getMinecraft().player.rotationYaw, Minecraft.getMinecraft().player.rotationPitch, Minecraft.getMinecraft().player.onGround));
        	Minecraft.getMinecraft().player.prevRotationPitch = Minecraft.getMinecraft().player.rotationPitch;
        	Minecraft.getMinecraft().player.prevRotationYaw = Minecraft.getMinecraft().player.rotationYaw;
    	}
    	
    	/*
    	if(trajectoryIsFlat) {
    		int charge = Minecraft.getMinecraft().player.getItemInUseCount();
    		float velocity = (float) charge / 20.0F;
    		velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
        	
    		if (velocity < 0.1D) {
    			return;
    		}
    		
    		if (velocity > 1.0F) {
    			velocity = 1.0F;
    		}
    		
    		double x = targetX - Minecraft.getMinecraft().player.posX;
    		double z = targetZ - Minecraft.getMinecraft().player.posZ;
    		double y = targetY - (Minecraft.getMinecraft().player.posY + Minecraft.getMinecraft().player.getEyeHeight());
    		double distance = Math.sqrt(x * x + z * z);
    		float gravity = 0.006F;
    		
    		Minecraft.getMinecraft().player.rotationYaw = (float) ((Math.atan2(z, x) * 180D) / Math.PI) - 90F;
            Minecraft.getMinecraft().player.rotationPitch = (float) -Math.toDegrees(Math.atan(((velocity * velocity) - Math.sqrt((velocity * velocity * velocity * velocity) - gravity * (gravity * (distance * distance) + 2 * y * (velocity * velocity)))) / (gravity * distance)));
    	} else {
    		float gravity = 0.006f;
        	float velocity = (72000 - Minecraft.getMinecraft().player.getItemInUseCount()) / 20F;
    		velocity = (velocity * velocity + velocity * 2) / 3;
    		if(velocity < 0.1D) return;
    		if(velocity > 1) velocity = 1;
        	double launchX = Minecraft.getMinecraft().player.posX;
        	double launchY = Minecraft.getMinecraft().player.posY + Minecraft.getMinecraft().player.getEyeHeight();
        	double launchZ = Minecraft.getMinecraft().player.posZ;
        	double differentialX = targetX - launchX;
        	double differentialZ = targetZ - launchZ;
        	double distance = Math.sqrt((differentialX * differentialX) + (differentialZ * differentialZ));
        	double height = Math.sqrt((targetY - launchY) * (targetY - launchY));
        	
        	//Azimuth
        	Minecraft.getMinecraft().player.rotationYaw = (float) Math.toDegrees(Math.atan2(differentialZ / (Math.sqrt((differentialX * differentialX) + (height * height) + (differentialZ * differentialZ))), differentialX / (Math.sqrt((differentialX * differentialX) + (height * height) + (differentialZ * differentialZ))))) - 90;
        	Minecraft.getMinecraft().player.rotationPitch = (float) -Math.toDegrees(Math.atan(((velocity * velocity) + Math.sqrt((velocity * velocity * velocity * velocity) - gravity * (gravity * (distance * distance) + 2 * height * (velocity * velocity))) / (gravity * distance))));
        	//Elevation
    	} */
		
    } 
    
    /*
    private float calculateVelocity(int charge) {
    	float energy = (float) charge / 20.0F;
    	energy = (energy * energy + energy * 2.0F) / 3.0F;
    	if(energy > 1.0F) {
    		energy = 1.0F;
    	}
    	energy *= 3.0F;
    	float moveX = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float moveY = -MathHelper.sin(pitch * 0.017453292F);
        float moveZ = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        
        float normalise = MathHelper.sqrt(moveX * moveX + moveY * moveY + moveZ * moveZ);
        
        moveX /= normalise;
        moveY /= normalise;
        moveZ /= normalise;
        
        moveX *= energy;
        moveY *= energy;
        moveZ *= energy;
        
        return MathHelper.sqrt(moveX * moveX + moveY * moveY + moveZ * moveZ) * 20;
        
    }*/
    
}
