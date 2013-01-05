/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.ExtraHardMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtraHardMode extends JavaPlugin
{
	//for convenience, a reference to the instance of this plugin
	public static ExtraHardMode instance;
	
	//for logging to the console and log file
	private static Logger log = Logger.getLogger("Minecraft");
	
	//this handles the config files (messages and plugin options)
	public DataStore dataStore;
	
	//for computing random chance
	static Random randomNumberGenerator = new Random();
	
	//configuration variables, loaded/saved from a config.yml
	public ArrayList<World> config_enabled_worlds;			        //list of worlds where extra hard mode rules apply
	
	//general monster rules
	public int config_moreMonstersMaxY;								//max y value for extra monster spawns
	public int config_moreMonstersMultiplier;						//what to multiply monster spawns by
	
	//monster grinder fix rules
	public boolean config_inhibitMonsterGrinders;					//whether monster grinders (or "farms") should be inhibited
	
	//world modification rules
	public int config_standardTorchMinY;							//minimum y for placing standard torches
	public boolean config_superHardStone;							//whether stone is hardened to encourage cave exploration over tunneling
	public boolean config_limitedBlockPlacement;					//whether players may place blocks directly underneath themselves
	public boolean config_betterTNT;								//whether TNT should be more powerful and plentiful
	public ArrayList<Material> config_moreFallingBlocks;			//which materials beyond sand and gravel should be subject to gravity
	
	//zombie rules
	public boolean config_zombiesDebilitatePlayers;					//whether zombies apply a debuff to players on hit
	public int config_zombiesReanimatePercent;						//percent chance for a zombie to reanimate after death
	
	//skeleton rules
	public int config_skeletonsKnockBackPercent;					//percent chance skeletons have a chance to knock back targets with arrows
	public int config_skeletonsReleaseSilverfishPercent;			//percent chance skeletons will release silverfish instead of firing arrows
	public int config_skeletonsDeflectArrowsPercent;				//whether or not arrows will pass harmlessly through skeletons
	
	//creeper rules
	public int config_chargedCreeperSpawnPercent;					//percentage of creepers which will spawn charged
	public boolean config_chargedCreepersExplodeOnHit;				//whether charged creepers explode when damaged
	
	//pig zombie rules
	public boolean config_alwaysAngryPigZombies;					//whether pig zombies are always hostile
	public boolean config_fortressPigsDropWart;						//whether pig zombies drop nether wart in nether fortresses
	
	//ghast rules
	public boolean config_ghastsDeflectArrows;						//whether ghasts should deflect arrows and drop extra loot
	
	//magma cube rules
	public boolean config_magmaCubesBecomeBlazesOnDamage;			//whether damaging a magma cube turns it into a blaze
	public int config_flameSlimesSpawnWithNetherBlazePercent;		//percentage chance that a blaze spawn will trigger a flame slime spawn as well
	
	//blaze rules
	public int config_bonusNetherBlazeSpawnPercent;					//percentage of pig zombies which will be replaced with blazes
	public boolean config_blazesExplodeOnDeath;						//whether blazes explode and spread fire when they die
	public int config_nearBedrockBlazeSpawnPercent;					//percentage of skeletons near bedrock which will be replaced with blazes
	public int config_netherBlazesSplitOnDeathPercent;				//percentage chance that a blaze slain in the nether will split into two blazes
	public boolean config_blazesDropFireOnDamage;					//whether blazes drop fire when damaged
	public boolean config_blazesDropBonusLoot;						//whether blazes drop extra loot
	
	//spider rules
	public int config_bonusUndergroundSpiderSpawnPercent;			//percentage of zombies which will be replaced with spiders under sea level
	public boolean config_spidersDropWebOnDeath;					//whether spiders drop webbing when they die
	
	//enderman rules
	public boolean config_improvedEndermanTeleportation;			//whether endermen may teleport players
	
	//melons and wheat
	public boolean config_seedReduction;							//whether melon and grass seeds are more rare
	
	//mushrooms
	public boolean config_noBonemealOnMushrooms;					//whether bonemeal may be used on mushrooms
	
	//nether wart
	public boolean config_noFarmingNetherWart;						//whether nether wart will ever drop more than 1 wart when broken
	
	//sheep (wool)
	public boolean config_sheepRegrowWhiteWool;						//whether sheep will always regrow white wool
	
	//water
	public boolean config_dontMoveWaterSourceBlocks;				//whether players may move water source blocks
	
	//player death
	public int config_playerRespawnHealth;							//how much health after respawn
	public int config_playerRespawnFoodLevel;						//how much food bar after respawn
	public int config_playerDeathItemStacksForfeitPercent;			//percentage of item stacks lost on death
	
	//player damage
	public boolean config_enhancedEnvironmentalDamage;				//whether players take additional damage and/or debuffs from environmental injuries
	public boolean config_extinguishingFireIgnitesPlayers;			//whether players catch fire when extinguishing a fire up close
	
	//explosions disable option, needed to dodge bugs in popular plugins
	public boolean config_workAroundExplosionsBugs;
	
	//adds a server log entry
	public static void AddLogEntry(String entry)
	{
		log.info("ExtraHardMode: " + entry);
	}
	
	//initializes well...   everything
	public void onEnable()
	{ 		
		AddLogEntry("Extra Hard Mode enabled.");
		
		this.dataStore = new DataStore();
		
		instance = this;
		
		//load the config if it exists
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(DataStore.configFilePath));
		
		//read configuration settings (note defaults), write back to config file
		
		//enabled worlds defaults
		ArrayList<String> defaultEnabledWorldNames = new ArrayList<String>();
		List<World> worlds = this.getServer().getWorlds(); 
		for(int i = 0; i < worlds.size(); i++)
		{
			defaultEnabledWorldNames.add(worlds.get(i).getName());
		}
		
		//get enabled world names from the config file
		List<String> enabledWorldNames = config.getStringList("ExtraHardMode.Worlds");
		if(enabledWorldNames == null || enabledWorldNames.size() == 0)
		{			
			enabledWorldNames = defaultEnabledWorldNames;
		}
		
		//validate enabled world names
		this.config_enabled_worlds = new ArrayList<World>();
		for(int i = 0; i < enabledWorldNames.size(); i++)
		{
			String worldName = enabledWorldNames.get(i);
			World world = this.getServer().getWorld(worldName);
			if(world == null)
			{
				AddLogEntry("Error: There's no world named \"" + worldName + "\".  Please update your config.yml.");
			}
			else
			{
				this.config_enabled_worlds.add(world);
			}
		}
		
		//write enabled world names to config file
		config.set("ExtraHardMode.Worlds", enabledWorldNames);
		
		this.config_standardTorchMinY = config.getInt("ExtraHardMode.PermanentFlameMinYCoord", 30);
		config.set("ExtraHardMode.PermanentFlameMinYCoord", this.config_standardTorchMinY);
		
		this.config_superHardStone = config.getBoolean("ExtraHardMode.HardenedStone", true);
		config.set("ExtraHardMode.HardenedStone", this.config_superHardStone);
		
		this.config_enhancedEnvironmentalDamage = config.getBoolean("ExtraHardMode.EnhancedEnvironmentalInjuries", true);
		config.set("ExtraHardMode.EnhancedEnvironmentalInjuries", this.config_enhancedEnvironmentalDamage);
		
		this.config_extinguishingFireIgnitesPlayers = config.getBoolean("ExtraHardMode.ExtinguishingFiresIgnitesPlayers", true);
		config.set("ExtraHardMode.ExtinguishingFiresIgnitesPlayers", this.config_extinguishingFireIgnitesPlayers);
		
		this.config_betterTNT = config.getBoolean("ExtraHardMode.BetterTNT", true);
		config.set("ExtraHardMode.BetterTNT", this.config_betterTNT);
		
		this.config_inhibitMonsterGrinders = config.getBoolean("ExtraHardMode.InhibitMonsterGrinders", true);
		config.set("ExtraHardMode.InhibitMonsterGrinders", this.config_inhibitMonsterGrinders);
		
		this.config_limitedBlockPlacement = config.getBoolean("ExtraHardMode.LimitedBlockPlacement", true);
		config.set("ExtraHardMode.LimitedBlockPlacement", this.config_limitedBlockPlacement);
		
		this.config_moreMonstersMaxY = config.getInt("ExtraHardMode.MoreMonsters.MaxYCoord", 55);
		config.set("ExtraHardMode.MoreMonsters.MaxYCoord", this.config_moreMonstersMaxY);
		
		this.config_moreMonstersMultiplier = config.getInt("ExtraHardMode.MoreMonsters.Multiplier", 2);
		config.set("ExtraHardMode.MoreMonsters.Multiplier", this.config_moreMonstersMultiplier);		
		
		this.config_zombiesDebilitatePlayers = config.getBoolean("ExtraHardMode.Zombies.SlowPlayers", true);
		config.set("ExtraHardMode.Zombies.SlowPlayers", this.config_zombiesDebilitatePlayers);
		
		this.config_zombiesReanimatePercent = config.getInt("ExtraHardMode.Zombies.ReanimatePercent", 50);
		config.set("ExtraHardMode.Zombies.ReanimatePercent", this.config_zombiesReanimatePercent);
		
		this.config_skeletonsKnockBackPercent = config.getInt("ExtraHardMode.Skeletons.ArrowsKnockBackPercent", 30);
		config.set("ExtraHardMode.Skeletons.ArrowsKnockBackPercent", this.config_skeletonsKnockBackPercent);
		
		this.config_skeletonsReleaseSilverfishPercent = config.getInt("ExtraHardMode.Skeletons.ReleaseSilverfishPercent", 30);
		config.set("ExtraHardMode.Skeletons.ReleaseSilverfishPercent", this.config_skeletonsReleaseSilverfishPercent);
		
		this.config_skeletonsDeflectArrowsPercent = config.getInt("ExtraHardMode.Skeletons.DeflectArrowsPercent", 100);
		config.set("ExtraHardMode.Skeletons.DeflectArrowsPercent", this.config_skeletonsDeflectArrowsPercent);
		
		this.config_bonusUndergroundSpiderSpawnPercent = config.getInt("ExtraHardMode.Spiders.BonusUndergroundSpawnPercent", 20);
		config.set("ExtraHardMode.Spiders.BonusUndergroundSpawnPercent", this.config_bonusUndergroundSpiderSpawnPercent);
		
		this.config_spidersDropWebOnDeath = config.getBoolean("ExtraHardMode.Spiders.DropWebOnDeath", true);
		config.set("ExtraHardMode.Spiders.DropWebOnDeath", this.config_spidersDropWebOnDeath);
		
		this.config_chargedCreeperSpawnPercent = config.getInt("ExtraHardMode.Creepers.ChargedCreeperSpawnPercent", 20);
		config.set("ExtraHardMode.Creepers.ChargedCreeperSpawnPercent", this.config_chargedCreeperSpawnPercent);
		
		this.config_chargedCreepersExplodeOnHit = config.getBoolean("ExtraHardMode.Creepers.ChargedCreepersExplodeOnDamage", true);
		config.set("ExtraHardMode.Creepers.ChargedCreepersExplodeOnDamage", this.config_chargedCreepersExplodeOnHit);
		
		this.config_nearBedrockBlazeSpawnPercent = config.getInt("ExtraHardMode.Blazes.NearBedrockSpawnPercent", 50);
		config.set("ExtraHardMode.Blazes.NearBedrockSpawnPercent", this.config_nearBedrockBlazeSpawnPercent);
		
		this.config_bonusNetherBlazeSpawnPercent = config.getInt("ExtraHardMode.Blazes.BonusNetherSpawnPercent", 20);
		config.set("ExtraHardMode.Blazes.BonusNetherSpawnPercent", this.config_bonusNetherBlazeSpawnPercent);
		
		this.config_flameSlimesSpawnWithNetherBlazePercent = config.getInt("ExtraHardMode.MagmaCubes.SpawnWithNetherBlazePercent", 100);
		config.set("ExtraHardMode.MagmaCubes.SpawnWithNetherBlazePercent", this.config_flameSlimesSpawnWithNetherBlazePercent);
		
		this.config_magmaCubesBecomeBlazesOnDamage = config.getBoolean("ExtraHardMode.MagmaCubes.GrowIntoBlazesOnDamage", true);
		config.set("ExtraHardMode.MagmaCubes.GrowIntoBlazesOnDamage", this.config_magmaCubesBecomeBlazesOnDamage);
		
		this.config_blazesExplodeOnDeath = config.getBoolean("ExtraHardMode.Blazes.ExplodeOnDeath", true);
		config.set("ExtraHardMode.Blazes.ExplodeOnDeath", this.config_blazesExplodeOnDeath);
		
		this.config_blazesDropFireOnDamage = config.getBoolean("ExtraHardMode.Blazes.DropFireOnDamage", true);
		config.set("ExtraHardMode.Blazes.DropFireOnDamage", this.config_blazesDropFireOnDamage);
		
		this.config_blazesDropBonusLoot = config.getBoolean("ExtraHardMode.Blazes.BonusLoot", true);
		config.set("ExtraHardMode.Blazes.BonusLoot", this.config_blazesDropBonusLoot);
		
		this.config_netherBlazesSplitOnDeathPercent = config.getInt("ExtraHardMode.Blazes.NetherSplitOnDeathPercent", 25);
		config.set("ExtraHardMode.Blazes.NetherSplitOnDeathPercent", this.config_netherBlazesSplitOnDeathPercent);
		
		this.config_alwaysAngryPigZombies = config.getBoolean("ExtraHardMode.PigZombies.AlwaysAngry", true);
		config.set("ExtraHardMode.PigZombies.AlwaysAngry", this.config_alwaysAngryPigZombies);
		
		this.config_fortressPigsDropWart = config.getBoolean("ExtraHardMode.PigZombies.DropWartInFortresses", true);
		config.set("ExtraHardMode.PigZombies.DropWartInFortresses", this.config_fortressPigsDropWart);
		
		this.config_ghastsDeflectArrows = config.getBoolean("ExtraHardMode.Ghasts.DeflectArrows", true);
		config.set("ExtraHardMode.Ghasts.DeflectArrows", this.config_ghastsDeflectArrows);
		
		this.config_improvedEndermanTeleportation = config.getBoolean("ExtraHardMode.Endermen.MayTeleportPlayers", true);
		config.set("ExtraHardMode.Endermen.MayTeleportPlayers", this.config_improvedEndermanTeleportation);
		
		this.config_seedReduction = config.getBoolean("ExtraHardMode.Farming.FewerSeeds", true);
		config.set("ExtraHardMode.Farming.FewerSeeds", this.config_seedReduction);
		
		this.config_noBonemealOnMushrooms = config.getBoolean("ExtraHardMode.Farming.NoBonemealOnMushrooms", true);
		config.set("ExtraHardMode.Farming.NoBonemealOnMushrooms", this.config_noBonemealOnMushrooms);		
		
		this.config_noFarmingNetherWart = config.getBoolean("ExtraHardMode.Farming.NoFarmingNetherWart", true);
		config.set("ExtraHardMode.Farming.NoFarmingNetherWart", this.config_noFarmingNetherWart);
		
		this.config_sheepRegrowWhiteWool = config.getBoolean("ExtraHardMode.Farming.SheepGrowOnlyWhiteWool", true);
		config.set("ExtraHardMode.Farming.SheepGrowOnlyWhiteWool", this.config_sheepRegrowWhiteWool);
		
		this.config_dontMoveWaterSourceBlocks = config.getBoolean("ExtraHardMode.Farming.BucketsDontMoveWaterSources", true);
		config.set("ExtraHardMode.Farming.BucketsDontMoveWaterSources", this.config_dontMoveWaterSourceBlocks);
		
		this.config_playerDeathItemStacksForfeitPercent = config.getInt("ExtraHardMode.PlayerDeath.ItemStacksForfeitPercent", 10);
		config.set("ExtraHardMode.PlayerDeath.ItemStacksForfeitPercent", this.config_playerDeathItemStacksForfeitPercent);
		
		this.config_playerRespawnHealth = config.getInt("ExtraHardMode.PlayerDeath.RespawnHealth", 15);
		config.set("ExtraHardMode.PlayerDeath.RespawnHealth", this.config_playerRespawnHealth);
		
		this.config_playerRespawnFoodLevel = config.getInt("ExtraHardMode.PlayerDeath.RespawnFoodLevel", 15);
		config.set("ExtraHardMode.PlayerDeath.RespawnFoodLevel", this.config_playerRespawnFoodLevel);
		
		this.config_workAroundExplosionsBugs = config.getBoolean("ExtraHardMode.WorkAroundOtherPluginsExplosionBugs", false);
		config.set("ExtraHardMode.WorkAroundOtherPluginsExplosionBugs", this.config_workAroundExplosionsBugs);
		
		
		//default additional falling blocks
		this.config_moreFallingBlocks = new ArrayList<Material>();
		this.config_moreFallingBlocks.add(Material.DIRT);
		this.config_moreFallingBlocks.add(Material.GRASS);
		this.config_moreFallingBlocks.add(Material.COBBLESTONE);
		this.config_moreFallingBlocks.add(Material.MOSSY_COBBLESTONE);
		this.config_moreFallingBlocks.add(Material.MYCEL);
		
		//build a default config entry for those blocks
		ArrayList<String> defaultMoreFallingBlocksList = new ArrayList<String>();
		for(int i = 0; i < this.config_moreFallingBlocks.size(); i++)
		{
			defaultMoreFallingBlocksList.add(this.config_moreFallingBlocks.get(i).name());
		}
		
		//try to load the list from the config file
		List<String> moreFallingBlocksList = config.getStringList("ExtraHardMode.AdditionalFallingBlocks");
		
		//if it fails, use the above default list instead
		if(moreFallingBlocksList == null || moreFallingBlocksList.size() == 0)
		{
			ExtraHardMode.AddLogEntry("Warning: The additional falling blocks list may not be empty.  If you don't want any additional falling blocks, list only a material which is never a block, like DIAMOND_SWORD.");
			moreFallingBlocksList = defaultMoreFallingBlocksList;
		}
		
		//parse this final list of additional falling blocks
		this.config_moreFallingBlocks = new ArrayList<Material>();
		for(int i = 0; i < moreFallingBlocksList.size(); i++)
		{
			String blockName = moreFallingBlocksList.get(i);
			Material material = Material.getMaterial(blockName);
			if(material == null)
			{
				ExtraHardMode.AddLogEntry("Additional Falling Blocks Configuration: Material not found: " + blockName + ".");
			}
			else
			{
				this.config_moreFallingBlocks.add(material);
			}
		}
		
		//write it back to the config
		config.set("ExtraHardMode.AdditionalFallingBlocks", moreFallingBlocksList);
		
		//save config values to file system
		try
		{
			config.save(DataStore.configFilePath);
		}
		catch(IOException exception)
		{
			AddLogEntry("Unable to write to the configuration file at \"" + DataStore.configFilePath + "\"");
		}
		
		//register for events
		PluginManager pluginManager = this.getServer().getPluginManager();
		
		//player events
		PlayerEventHandler playerEventHandler = new PlayerEventHandler();
		pluginManager.registerEvents(playerEventHandler, this);
		
		//block events
		BlockEventHandler blockEventHandler = new BlockEventHandler();
		pluginManager.registerEvents(blockEventHandler, this);
				
		//entity events
		EntityEventHandler entityEventHandler = new EntityEventHandler();
		pluginManager.registerEvents(entityEventHandler, this);
		
		//FEATURE: triple result from TNT recipe
		if(this.config_betterTNT)
		{
			Iterator<Recipe> recipeIterator = this.getServer().recipeIterator();
			while(recipeIterator.hasNext())
			{
				Recipe recipe = recipeIterator.next();
				if(recipe.getResult().getType() == Material.TNT && recipe instanceof ShapedRecipe && recipe.getResult().getAmount() == 1)
				{
					recipeIterator.remove();
				}
			}
			
			ShapedRecipe newRecipe = new ShapedRecipe(new ItemStack(Material.TNT, 3));
			newRecipe.shape("GSG", "SGS", "GSG");
			newRecipe.setIngredient('S', Material.SAND);
			newRecipe.setIngredient('G', Material.SULPHUR);
			
			this.getServer().addRecipe(newRecipe);
		}
		
		//FEATURE: can't get melon seeds from melon slices
		if(this.config_seedReduction)
		{
			Iterator<Recipe> recipeIterator = this.getServer().recipeIterator();
			while(recipeIterator.hasNext())
			{
				Recipe recipe = recipeIterator.next();
				if(recipe.getResult().getType() == Material.MELON_SEEDS)
				{
					recipeIterator.remove();
				}
			}			
		}		
	}
	
	//handles slash commands
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return true;
	}
	
	public void onDisable()
	{ 
		AddLogEntry("ExtraHardMode disabled.");
	}
	
	//sends a color-coded message to a player
	static void sendMessage(Player player, ChatColor color, Messages messageID, String... args)
	{
		String message = ExtraHardMode.instance.dataStore.getMessage(messageID, args);
		sendMessage(player, color, message);
	}
	
	//sends a color-coded message to a player
	static void sendMessage(Player player, ChatColor color, String message)
	{
		if(player == null)
		{
			ExtraHardMode.AddLogEntry(color + message);
		}
		else
		{
			player.sendMessage(color + message);
		}
	}	
	
	static void physicsCheck(Block block, int recursionCount, boolean skipCenterBlock)
	{
		ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(
				ExtraHardMode.instance,
				new BlockPhysicsCheckTask(block, recursionCount),
				5L);
	}
	
	//makes a block subject to gravity
	static void applyPhysics(Block block)
	{
		//grass and mycel become dirt when they fall
		if(block.getType() == Material.GRASS || block.getType() == Material.MYCEL)
		{
			block.setType(Material.DIRT);
		}
		
		//create falling block
		FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getTypeId(), block.getData());
		fallingBlock.setDropItem(false);
		
		//remove original block
		block.setType(Material.AIR);
	}
	
	//computes random chance
	static boolean random(int percentChance)
	{
		return randomNumberGenerator.nextInt(101) < percentChance;
	}
}