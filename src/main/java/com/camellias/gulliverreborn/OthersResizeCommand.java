package com.camellias.gulliverreborn;

import java.util.List;
import java.util.UUID;

import com.artemis.artemislib.util.attributes.ArtemisLibAttributes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class OthersResizeCommand extends CommandBase
{
	private final List<String> aliases = Lists.newArrayList(GulliverReborn.MODID, "basesize", "bs");
	private static UUID uuidHeight = UUID.fromString("5440b01a-974f-4495-bb9a-c7c87424bca4");
	private static UUID uuidWidth = UUID.fromString("3949d2ed-b6cc-4330-9c13-98777f48ea51");
	private static UUID uuidReach1 = UUID.fromString("854e0004-c218-406c-a9e2-590f1846d80b");
	private static UUID uuidReach2 = UUID.fromString("216080dc-22d3-4eff-a730-190ec0210d5c");
	private static UUID uuidHealth = UUID.fromString("3b901d47-2d30-495c-be45-f0091c0f6fb2");
	private static UUID uuidStrength = UUID.fromString("558f55be-b277-4091-ae9b-056c7bc96e84");
	private static UUID uuidSpeed = UUID.fromString("f2fb5cda-3fbe-4509-a0af-4fc994e6aeca");
	
	@Override
	public String getName() 
	{
		return "basesize";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "basesize <player> <id>";
	}
	
	@Override
	public List<String> getAliases()
	{
		return aliases;
	}
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length < 1) return;
		
		String s = args[1];
		float size= Float.parseFloat(s);
		
		EntityPlayer player = getPlayer(server, sender, args[0]);
		
		size = MathHelper.clamp(size, 0.25F, Config.MAX_SIZE);
		Multimap<String, AttributeModifier> attributes = HashMultimap.create();
		Multimap<String, AttributeModifier> removeableAttributes = HashMultimap.create();
		Multimap<String, AttributeModifier> removeableAttributes2 = HashMultimap.create();
		
		attributes.put(ArtemisLibAttributes.ENTITY_HEIGHT.getName(), new AttributeModifier(uuidHeight, "Player Height", size - 1, 2));
		if(Config.SMALL_IS_CHONK) attributes.put(ArtemisLibAttributes.ENTITY_WIDTH.getName(), new AttributeModifier(uuidWidth, "Player Width", MathHelper.clamp(size - 1, 0.4 - 1, Config.MAX_SIZE), 2));
		if(!Config.SMALL_IS_CHONK) attributes.put(ArtemisLibAttributes.ENTITY_WIDTH.getName(), new AttributeModifier(uuidWidth, "Player Width", size - 1, 2));
		
		if(Config.SPEED_MODIFIER) attributes.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(uuidSpeed, "Player Speed", (size - 1) / 2, 2));
		if(Config.REACH_MODIFIER) removeableAttributes.put(EntityPlayer.REACH_DISTANCE.getName(), new AttributeModifier(uuidReach1, "Player Reach 1", size - 1, 2));
		if(Config.REACH_MODIFIER) removeableAttributes2.put(EntityPlayer.REACH_DISTANCE.getName(), new AttributeModifier(uuidReach2, "Player Reach 2", -MathHelper.clamp(size - 1, 0.33, Double.MAX_VALUE), 2));
		if(Config.STRENGTH_MODIFIER) attributes.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(uuidStrength, "Player Strength", size - 1, 0));
		if(Config.HEALTH_MODIFIER) attributes.put(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier(uuidHealth, "Player Health", (size - 1) * Config.HEALTH_MULTIPLIER, 2));
		
		if(size > 1)
		{
			player.getAttributeMap().applyAttributeModifiers(removeableAttributes);
		}
		else
		{
			player.getAttributeMap().removeAttributeModifiers(removeableAttributes);
		}
		
		if(size < 1)
		{
			player.getAttributeMap().applyAttributeModifiers(removeableAttributes2);
		}
		else
		{
			player.getAttributeMap().removeAttributeModifiers(removeableAttributes2);
		}
		
		player.getAttributeMap().applyAttributeModifiers(attributes);
		
		player.setHealth(player.getMaxHealth());
		
		if(sender instanceof EntityPlayer) GulliverReborn.LOGGER.info(((EntityPlayer) sender).getDisplayNameString() + " set " + player.getDisplayNameString() +"'s size to " + size);
	}
}
