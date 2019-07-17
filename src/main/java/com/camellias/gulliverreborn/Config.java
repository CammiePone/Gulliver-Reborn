package com.camellias.gulliverreborn;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config
{
	public static Configuration config;
	
	public static float MAX_SIZE;
	public static float HEALTH_MULTIPLIER;
	public static boolean SMALL_IS_CHONK;
	public static boolean SPEED_MODIFIER;
	public static boolean REACH_MODIFIER;
	public static boolean STRENGTH_MODIFIER;
	public static boolean HEALTH_MODIFIER;
	
	public static void init(File file)
	{
		config = new Configuration(file);
		
		String category;
		
		category = "Gulliver Reborn Config Options";
		config.addCustomCategoryComment(category, "");
		
		MAX_SIZE = config.getFloat("Set the maximum player size", category, Float.MAX_VALUE, 0.25F, Float.MAX_VALUE, "Max player size");
		HEALTH_MULTIPLIER = config.getFloat("Set the health multiplier", category, 1.0F, Float.MIN_VALUE, Float.MAX_VALUE, "Health Multiplier");
		
		SMALL_IS_CHONK = config.getBoolean("Do you want skinny guys and clipping, or fat guys and no clipping?", category, true, "Small is chonk?");
		SPEED_MODIFIER = config.getBoolean("Enable/disable the speed modifier", category, true, "Speed changes on resize");
		REACH_MODIFIER = config.getBoolean("Enable/disable the reach modifier", category, true, "Reach distance changes on resize");
		STRENGTH_MODIFIER = config.getBoolean("Enable/disable the strength modifier", category, true, "Strength changes on resize");
		HEALTH_MODIFIER = config.getBoolean("Enable/disable the health modifier", category, true, "Health changes on resize");
		
		config.save();
	}
	
	public static void registerConfig(FMLPreInitializationEvent event)
	{
		GulliverReborn.config = new File(event.getModConfigurationDirectory() + "/" + GulliverReborn.MODID);
		GulliverReborn.config.mkdirs();
		init(new File(GulliverReborn.config.getPath(), GulliverReborn.MODID + ".cfg"));
	}
}
