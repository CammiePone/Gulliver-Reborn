package com.camellias.gulliverreborn;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
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
	public static final String VERSION = "1.0";
	public static final String MCVERSION = "1.12.2";
	public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2795,];" + "required-after:artemislib@[1.0.0,];";
	public static final DamageSource CRUSHING = new DamageSource(MODID + ".crushing");
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
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
	public void onPlayerTick(PlayerTickEvent event)
	{
		EntityPlayer player = event.player;
		World world = event.player.world;
		
		player.stepHeight = player.height / 3F;
		
		if(player.height <= 0.45F)
		{
			for(ItemStack stack : player.getHeldEquipment())
			{
				if(stack.getItem() == Items.SLIME_BALL || stack.getItem() == Item.getItemFromBlock(Blocks.SLIME_BLOCK))
				{
					if(ClimbingHandler.canClimb(player, player.getHorizontalFacing()))
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
						player.jumpMovementFactor *= 1.75F;
						
						if(player.motionY < 0D)
						{
							player.motionY *= 0.6D;
						}
						
						for(double blockY = player.posY; 
								((world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.AIR) ||
								(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.LAVA)||
								(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.FIRE)||
								(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.LIT_FURNACE)) &&
								player.posY - blockY < 25; 
								blockY--)
						{
							if((world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.LAVA)||
									(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.FIRE)||
									(world.getBlockState(new BlockPos(player.posX, blockY, player.posZ)).getBlock() == Blocks.LIT_FURNACE))
							{
								player.motionY += MathHelper.clamp(0.1D, Double.MIN_VALUE, 0.1D);
								player.fallDistance = 0;
							}
						}
					}
				}
			}
		}
		
		for(EntityLivingBase entities : world.getEntitiesWithinAABB(EntityLivingBase.class, player.getEntityBoundingBox()))
		{
			if(!player.isSneaking())
			{
				if(player.height / entities.height >= 4)
				{
					entities.attackEntityFrom(CRUSHING, player.height - entities.height);
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
			
			if(target.height / 2 <= player.height)
			{
				target.startRiding(player);
			}
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
}
