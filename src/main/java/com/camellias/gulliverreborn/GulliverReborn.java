package com.camellias.gulliverreborn;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockClay;
import net.minecraft.block.BlockConcretePowder;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockRedFlower;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

@Mod(
modid = GulliverReborn.MODID,
name = GulliverReborn.NAME,
version = GulliverReborn.VERSION,
acceptedMinecraftVersions = GulliverReborn.MCVERSION,
dependencies = GulliverReborn.DEPENDENCIES)
public class GulliverReborn
{
	public static final String MODID = "gulliverreborn";
	public static final String NAME = "Gulliver Reborn";
	public static final String VERSION = "1.3";
	public static final String MCVERSION = "1.12.2";
	public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2795,];" + "required-after:artemislib@[1.0.6,];";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	public static File config;
	
	public static DamageSource causeCrushingDamage(EntityLivingBase entity)
	{
		return new EntityDamageSource(MODID + ".crushing", entity);
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.registerConfig(event);
		MinecraftForge.EVENT_BUS.register(new GulliverReborn());
	}
	
	@EventHandler
	public void serverRegistries(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new MyResizeCommand());
		event.registerServerCommand(new OthersResizeCommand());
	}
	
	@SubscribeEvent
	public void onPlayerDamaged(LivingFallEvent event)
	{
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			
			event.setDistance(event.getDistance() / (player.height * 0.6F));
		}
	}
	
	@SubscribeEvent
	public void onLivingTick(LivingUpdateEvent event)
	{
		EntityLivingBase entity = event.getEntityLiving();
		World world = event.getEntityLiving().world;
		
		for(EntityLivingBase entities : world.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox()))
		{
			if(!entity.isSneaking())
			{
				if(entity.height / entities.height >= 4)
				{
					entities.attackEntityFrom(causeCrushingDamage(entity), entity.height - entities.height);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onTargetEntity(LivingSetAttackTargetEvent event)
	{
		if(event.getTarget() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityLiving)
		{
			EntityPlayer player = (EntityPlayer) event.getTarget();
			EntityLiving entity = (EntityLiving) event.getEntityLiving();
			
			if(!(entity instanceof EntitySpider || entity instanceof EntityOcelot))
			{
				if(player.height <= 0.45F)
				{
					entity.setAttackTarget(null);
				}
			}
			else
			{
				if(player.height <= 0.45F)
				{
					entity.setAttackTarget(player);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		EntityPlayer player = event.player;
		World world = event.player.world;
		
		player.stepHeight = player.height / 3F;
		player.jumpMovementFactor *= (player.height / 1.8F);
		
		if(player.height < 0.9F)
		{
			BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			float ratio = (player.height / 1.8F) / 2;
			
			if(block instanceof BlockRedFlower || state == Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.ROSE))
			{
				player.attackEntityFrom(DamageSource.CACTUS, 1);
			}
			
			if(!player.capabilities.isFlying
				&& (block instanceof BlockBush)
				|| (block instanceof BlockCarpet)
				|| (block instanceof BlockFlower)
				|| (block instanceof BlockReed)
				|| (block instanceof BlockSnow)
				|| (block instanceof BlockWeb)
				|| (block instanceof BlockSoulSand))
			{
				player.motionX *= ratio;
				if(block instanceof BlockWeb) player.motionY *= ratio;
				player.motionZ *= ratio;
			}
		}
		
		if(player.height <= 0.45F)
		{
			EnumFacing facing = player.getHorizontalFacing();
			BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
			IBlockState state = world.getBlockState(pos.add(0, 0, 0).offset(facing));
			Block block = state.getBlock();
			boolean canPass = block.isPassable(world, pos.offset(facing));
			
			if(ClimbingHandler.canClimb(player, facing)
				&& (block instanceof BlockDirt)
				|| (block instanceof BlockGrass)
				|| (block instanceof BlockMycelium)
				|| (block instanceof BlockLeaves)
				|| (block instanceof BlockSand)
				|| (block instanceof BlockSoulSand)
				|| (block instanceof BlockConcretePowder)
				|| (block instanceof BlockFarmland)
				|| (block instanceof BlockGrassPath)
				|| (block instanceof BlockGravel)
				|| (block instanceof BlockClay))
			{
				if (player.collidedHorizontally)
				{
					if (!player.isSneaking())
					{
						player.motionY = 0.1D;
					}
					
					if (player.isSneaking())
					{
						player.motionY = 0.0D;
					}
				}
			}
			
			for(ItemStack stack : player.getHeldEquipment())
			{
				if(stack.getItem() == Items.SLIME_BALL || stack.getItem() == Item.getItemFromBlock(Blocks.SLIME_BLOCK))
				{
					if(ClimbingHandler.canClimb(player, facing))
					{
						if (player.collidedHorizontally)
						{
							if (!player.isSneaking())
							{
								player.motionY = 0.1D;
							}
							
							if (player.isSneaking())
							{
								player.motionY = 0.0D;
							}
						}
					}
				}
				
				if(stack.getItem() == Items.PAPER)
				{
					if(!player.onGround)
					{
						player.jumpMovementFactor = 0.02F * 1.75F;
						player.fallDistance = 0;
						
						if(player.motionY < 0D)
						{
							player.motionY *= 0.6D;
						}
						
						if(player.isSneaking())
						{
							player.jumpMovementFactor *= 3.50F;
						}
						
						for(double blockY = player.posY; !player.isSneaking() &&
								((world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.AIR) ||
								(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.LAVA) ||
								(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.FIRE) ||
								(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.LIT_FURNACE) ||
								(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.MAGMA)) &&
								player.posY - blockY < 25;
								blockY--)
						{
							if((world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.LAVA)||
									(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.FIRE)||
									(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.LIT_FURNACE) ||
									(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.MAGMA))
							{
								player.motionY += MathHelper.clamp(0.07D, Double.MIN_VALUE, 0.1D);
							}
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteract event)
	{
		if(event.getTarget() instanceof EntityLivingBase)
		{
			EntityLivingBase target = (EntityLivingBase) event.getTarget();
			EntityPlayer player = event.getEntityPlayer();
			
			if(target.height / 2 >= player.height)
			{
				for(ItemStack stack : player.getHeldEquipment())
				{
					if(stack.getItem() == Items.STRING)
					{
						player.startRiding(target);
					}
				}
			}
			
			if(target.height * 2 <= player.height)
			{
				target.startRiding(player);
			}
			
			if(player.getHeldItemMainhand().isEmpty() && player.isBeingRidden() && player.isSneaking())
			{
				for(Entity entities : player.getPassengers())
				{
					if(entities instanceof EntityLivingBase)
					{
						entities.dismountRidingEntity();
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onFOVChange(FOVUpdateEvent event)
	{
		if(event.getEntity() != null)
		{
			EntityPlayer player = event.getEntity();
			GameSettings settings = Minecraft.getMinecraft().gameSettings;
			PotionEffect speed = player.getActivePotionEffect(MobEffects.SPEED);
			float fov = settings.fovSetting / settings.fovSetting;
			
			event.setNewfov(speed != null ? fov + (10 * (speed.getAmplifier() + 1)) : fov);
		}
	}
	
	@SubscribeEvent
	public void onEntityJump(LivingJumpEvent event)
	{
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			float jumpHeight = (player.height / 1.8F);
			
			jumpHeight = MathHelper.clamp(jumpHeight, 0.65F, jumpHeight);
			player.motionY *= jumpHeight;
			
			if(player.isSneaking() || player.isSprinting())
			{
				if(player.height < 1.8F) player.motionY = 0.42F;
			}
		}
	}
	
	@SubscribeEvent
	public void onHarvest(BreakSpeed event)
	{
		EntityPlayer player = event.getEntityPlayer();
		
		event.setNewSpeed(event.getOriginalSpeed() * (player.height / 1.8F));
	}
	
	@SubscribeEvent
	public void onCameraSetup(CameraSetup event)
	{
		EntityPlayer player = Minecraft.getMinecraft().player;
		float scale = player.height / 1.8F;
		
		if(Minecraft.getMinecraft().gameSettings.thirdPersonView == 1)
		{
			if(player.height > 1.8F) GL11.glTranslatef(0, 0, -scale * 2);
		}
		
		if(Minecraft.getMinecraft().gameSettings.thirdPersonView == 2)
		{
			if(player.height > 1.8F) GL11.glTranslatef(0, 0, scale * 2);
		}
	}
}
