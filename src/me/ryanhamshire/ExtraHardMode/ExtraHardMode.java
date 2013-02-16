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
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
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
	public int config_monsterSpawnsInLightMaxY;						//max y value for monsters to spawn in the light
	
	//monster grinder fix rules
	public boolean config_inhibitMonsterGrinders;					//whether monster grinders (or "farms") should be inhibited
	
	//world modification rules
	public int config_standardTorchMinY;							//minimum y for placing standard torches
	public boolean config_superHardStone;							//whether stone is hardened to encourage cave exploration over tunneling
	public boolean config_limitedBlockPlacement;					//whether players may place blocks directly underneath themselves
	public boolean config_betterTNT;								//whether TNT should be more powerful and plentiful
	public ArrayList<Material> config_moreFallingBlocks;			//which materials beyond sand and gravel should be subject to gravity
	public boolean config_limitedTorchPlacement;					//whether players are limited to placing torches against specific materials
	public boolean config_rainBreaksTorches;						//whether rain should break torches
	public int config_brokenNetherrackCatchesFirePercent;			//percent chance for broken netherrack to start a fire
	
	//zombie rules
	public boolean config_zombiesDebilitatePlayers;					//whether zombies apply a debuff to players on hit
	public int config_zombiesReanimatePercent;						//percent chance for a zombie to reanimate after death
	
	//skeleton rules
	public int config_skeletonsKnockBackPercent;					//percent chance skeletons have a chance to knock back targets with arrows
	public int config_skeletonsReleaseSilverfishPercent;			//percent chance skeletons will release silverfish instead of firing arrows
	public int config_skeletonsDeflectArrowsPercent;				//whether or not arrows will pass harmlessly through skeletons
	public boolean config_flamingCreepersExplode;					//whether creepers explode when caught on fire
	
	//creeper rules
	public int config_chargedCreeperSpawnPercent;					//percentage of creepers which will spawn charged
	public boolean config_chargedCreepersExplodeOnHit;				//whether charged creepers explode when damaged
	public int config_creepersDropTNTOnDeathPercent;				//percentage of creepers which spawn activated TNT on death
	
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
	
	//witch rules
	public int config_bonusWitchSpawnPercent;						//percentage of surface zombies which spawn as witches
	
	//ender dragon rules
	public boolean config_respawnEnderDragon;						//whether the ender dragon respawns
	public boolean config_enderDragonDropsEgg;						//whether it drops an egg when slain
	public boolean config_enderDragonDropsVillagerEggs;				//whether it drops a pair of villager eggs when slain
	public boolean config_enderDragonAdditionalAttacks;				//whether the dragon spits fireballs and summons minions
	public boolean config_enderDragonCombatAnnouncements;			//whether server wide messages will broadcast player victories and defeats
	public boolean config_enderDragonNoBuilding;					//whether players will be allowed to build in the end
	
	//melons and wheat
	public boolean config_weakFoodCrops;							//whether food crops die more easily
	
	//mushrooms
	public boolean config_noBonemealOnMushrooms;					//whether bonemeal may be used on mushrooms
	
	//nether wart
	public boolean config_noFarmingNetherWart;						//whether nether wart will ever drop more than 1 wart when broken
	
	//sheep (wool)
	public boolean config_sheepRegrowWhiteWool;						//whether sheep will always regrow white wool
	
	//water
	public boolean config_dontMoveWaterSourceBlocks;				//whether players may move water source blocks
	public boolean config_noSwimmingInArmor;						//whether players may swim while wearing armor
	
	//player death
	public int config_playerRespawnHealth;							//how much health after respawn
	public int config_playerRespawnFoodLevel;						//how much food bar after respawn
	public int config_playerDeathItemStacksForfeitPercent;			//percentage of item stacks lost on death
	
	//player damage
	public boolean config_enhancedEnvironmentalDamage;				//whether players take additional damage and/or debuffs from environmental injuries
	public boolean config_extinguishingFireIgnitesPlayers;			//whether players catch fire when extinguishing a fire up close
	
	//tree felling
	public boolean config_betterTreeChopping;						//whether tree logs respect gravity
	
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
		FileConfiguration outConfig = new YamlConfiguration();
		
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
		outConfig.set("ExtraHardMode.Worlds", enabledWorldNames);
		
		this.config_standardTorchMinY = config.getInt("ExtraHardMode.PermanentFlameMinYCoord", 30);
		outConfig.set("ExtraHardMode.PermanentFlameMinYCoord", this.config_standardTorchMinY);
		
		this.config_superHardStone = config.getBoolean("ExtraHardMode.HardenedStone", true);
		outConfig.set("ExtraHardMode.HardenedStone", this.config_superHardStone);
		
		this.config_enhancedEnvironmentalDamage = config.getBoolean("ExtraHardMode.EnhancedEnvironmentalInjuries", true);
		outConfig.set("ExtraHardMode.EnhancedEnvironmentalInjuries", this.config_enhancedEnvironmentalDamage);
		
		this.config_extinguishingFireIgnitesPlayers = config.getBoolean("ExtraHardMode.ExtinguishingFiresIgnitesPlayers", true);
		outConfig.set("ExtraHardMode.ExtinguishingFiresIgnitesPlayers", this.config_extinguishingFireIgnitesPlayers);
		
		this.config_betterTNT = config.getBoolean("ExtraHardMode.BetterTNT", true);
		outConfig.set("ExtraHardMode.BetterTNT", this.config_betterTNT);
		
		this.config_inhibitMonsterGrinders = config.getBoolean("ExtraHardMode.InhibitMonsterGrinders", true);
		outConfig.set("ExtraHardMode.InhibitMonsterGrinders", this.config_inhibitMonsterGrinders);
		
		this.config_limitedBlockPlacement = config.getBoolean("ExtraHardMode.LimitedBlockPlacement", true);
		outConfig.set("ExtraHardMode.LimitedBlockPlacement", this.config_limitedBlockPlacement);
		
		this.config_limitedTorchPlacement = config.getBoolean("ExtraHardMode.LimitedTorchPlacement", true);
		outConfig.set("ExtraHardMode.LimitedTorchPlacement", this.config_limitedTorchPlacement);
		
		this.config_rainBreaksTorches = config.getBoolean("ExtraHardMode.RainBreaksTorches", true);
		outConfig.set("ExtraHardMode.RainBreaksTorches", this.config_rainBreaksTorches);		
		
		this.config_brokenNetherrackCatchesFirePercent = config.getInt("ExtraHardMode.NetherrackCatchesFirePercent", 20);
		outConfig.set("ExtraHardMode.NetherrackCatchesFirePercent", this.config_brokenNetherrackCatchesFirePercent);		
		
		this.config_moreMonstersMaxY = config.getInt("ExtraHardMode.MoreMonsters.MaxYCoord", 55);
		outConfig.set("ExtraHardMode.MoreMonsters.MaxYCoord", this.config_moreMonstersMaxY);
		
		this.config_moreMonstersMultiplier = config.getInt("ExtraHardMode.MoreMonsters.Multiplier", 2);
		outConfig.set("ExtraHardMode.MoreMonsters.Multiplier", this.config_moreMonstersMultiplier);
		
		this.config_monsterSpawnsInLightMaxY = config.getInt("ExtraHardMode.MonstersSpawnInLightMaxY", 50);
		outConfig.set("ExtraHardMode.MonstersSpawnInLightMaxY", this.config_monsterSpawnsInLightMaxY);		
		
		this.config_zombiesDebilitatePlayers = config.getBoolean("ExtraHardMode.Zombies.SlowPlayers", true);
		outConfig.set("ExtraHardMode.Zombies.SlowPlayers", this.config_zombiesDebilitatePlayers);
		
		this.config_zombiesReanimatePercent = config.getInt("ExtraHardMode.Zombies.ReanimatePercent", 50);
		outConfig.set("ExtraHardMode.Zombies.ReanimatePercent", this.config_zombiesReanimatePercent);
		
		this.config_skeletonsKnockBackPercent = config.getInt("ExtraHardMode.Skeletons.ArrowsKnockBackPercent", 30);
		outConfig.set("ExtraHardMode.Skeletons.ArrowsKnockBackPercent", this.config_skeletonsKnockBackPercent);
		
		this.config_skeletonsReleaseSilverfishPercent = config.getInt("ExtraHardMode.Skeletons.ReleaseSilverfishPercent", 30);
		outConfig.set("ExtraHardMode.Skeletons.ReleaseSilverfishPercent", this.config_skeletonsReleaseSilverfishPercent);
		
		this.config_skeletonsDeflectArrowsPercent = config.getInt("ExtraHardMode.Skeletons.DeflectArrowsPercent", 100);
		outConfig.set("ExtraHardMode.Skeletons.DeflectArrowsPercent", this.config_skeletonsDeflectArrowsPercent);
		
		this.config_bonusUndergroundSpiderSpawnPercent = config.getInt("ExtraHardMode.Spiders.BonusUndergroundSpawnPercent", 20);
		outConfig.set("ExtraHardMode.Spiders.BonusUndergroundSpawnPercent", this.config_bonusUndergroundSpiderSpawnPercent);
		
		this.config_spidersDropWebOnDeath = config.getBoolean("ExtraHardMode.Spiders.DropWebOnDeath", true);
		outConfig.set("ExtraHardMode.Spiders.DropWebOnDeath", this.config_spidersDropWebOnDeath);
		
		this.config_bonusWitchSpawnPercent = config.getInt("ExtraHardMode.Witches.BonusSpawnPercent", 5);
		outConfig.set("ExtraHardMode.Witches.BonusSpawnPercent", this.config_bonusWitchSpawnPercent);
		
		this.config_chargedCreeperSpawnPercent = config.getInt("ExtraHardMode.Creepers.ChargedCreeperSpawnPercent", 20);
		outConfig.set("ExtraHardMode.Creepers.ChargedCreeperSpawnPercent", this.config_chargedCreeperSpawnPercent);
		
		this.config_creepersDropTNTOnDeathPercent = config.getInt("ExtraHardMode.Creepers.DropTNTOnDeathPercent", 20);
		outConfig.set("ExtraHardMode.Creepers.DropTNTOnDeathPercent", this.config_creepersDropTNTOnDeathPercent);
		
		this.config_chargedCreepersExplodeOnHit = config.getBoolean("ExtraHardMode.Creepers.ChargedCreepersExplodeOnDamage", true);
		outConfig.set("ExtraHardMode.Creepers.ChargedCreepersExplodeOnDamage", this.config_chargedCreepersExplodeOnHit);
		
		this.config_flamingCreepersExplode = config.getBoolean("ExtraHardMode.Creepers.FireTriggersExplosion", true);
		outConfig.set("ExtraHardMode.Creepers.FireTriggersExplosion", this.config_flamingCreepersExplode);
		
		this.config_nearBedrockBlazeSpawnPercent = config.getInt("ExtraHardMode.Blazes.NearBedrockSpawnPercent", 50);
		outConfig.set("ExtraHardMode.Blazes.NearBedrockSpawnPercent", this.config_nearBedrockBlazeSpawnPercent);
		
		this.config_bonusNetherBlazeSpawnPercent = config.getInt("ExtraHardMode.Blazes.BonusNetherSpawnPercent", 20);
		outConfig.set("ExtraHardMode.Blazes.BonusNetherSpawnPercent", this.config_bonusNetherBlazeSpawnPercent);
		
		this.config_flameSlimesSpawnWithNetherBlazePercent = config.getInt("ExtraHardMode.MagmaCubes.SpawnWithNetherBlazePercent", 100);
		outConfig.set("ExtraHardMode.MagmaCubes.SpawnWithNetherBlazePercent", this.config_flameSlimesSpawnWithNetherBlazePercent);
		
		this.config_magmaCubesBecomeBlazesOnDamage = config.getBoolean("ExtraHardMode.MagmaCubes.GrowIntoBlazesOnDamage", true);
		outConfig.set("ExtraHardMode.MagmaCubes.GrowIntoBlazesOnDamage", this.config_magmaCubesBecomeBlazesOnDamage);
		
		this.config_blazesExplodeOnDeath = config.getBoolean("ExtraHardMode.Blazes.ExplodeOnDeath", true);
		outConfig.set("ExtraHardMode.Blazes.ExplodeOnDeath", this.config_blazesExplodeOnDeath);
		
		this.config_blazesDropFireOnDamage = config.getBoolean("ExtraHardMode.Blazes.DropFireOnDamage", true);
		outConfig.set("ExtraHardMode.Blazes.DropFireOnDamage", this.config_blazesDropFireOnDamage);
		
		this.config_blazesDropBonusLoot = config.getBoolean("ExtraHardMode.Blazes.BonusLoot", true);
		outConfig.set("ExtraHardMode.Blazes.BonusLoot", this.config_blazesDropBonusLoot);
		
		this.config_netherBlazesSplitOnDeathPercent = config.getInt("ExtraHardMode.Blazes.NetherSplitOnDeathPercent", 25);
		outConfig.set("ExtraHardMode.Blazes.NetherSplitOnDeathPercent", this.config_netherBlazesSplitOnDeathPercent);
		
		this.config_alwaysAngryPigZombies = config.getBoolean("ExtraHardMode.PigZombies.AlwaysAngry", true);
		outConfig.set("ExtraHardMode.PigZombies.AlwaysAngry", this.config_alwaysAngryPigZombies);
		
		this.config_fortressPigsDropWart = config.getBoolean("ExtraHardMode.PigZombies.DropWartInFortresses", true);
		outConfig.set("ExtraHardMode.PigZombies.DropWartInFortresses", this.config_fortressPigsDropWart);
		
		this.config_ghastsDeflectArrows = config.getBoolean("ExtraHardMode.Ghasts.DeflectArrows", true);
		outConfig.set("ExtraHardMode.Ghasts.DeflectArrows", this.config_ghastsDeflectArrows);
		
		this.config_improvedEndermanTeleportation = config.getBoolean("ExtraHardMode.Endermen.MayTeleportPlayers", true);
		outConfig.set("ExtraHardMode.Endermen.MayTeleportPlayers", this.config_improvedEndermanTeleportation);
		
		this.config_respawnEnderDragon = config.getBoolean("ExtraHardMode.EnderDragon.Respawns", true);
		outConfig.set("ExtraHardMode.EnderDragon.Respawns", this.config_respawnEnderDragon);		
		
		this.config_enderDragonDropsEgg = config.getBoolean("ExtraHardMode.EnderDragon.DropsEgg", true);
		outConfig.set("ExtraHardMode.EnderDragon.DropsEgg", this.config_enderDragonDropsEgg);
		
		this.config_enderDragonDropsVillagerEggs = config.getBoolean("ExtraHardMode.EnderDragon.DropsVillagerEggs", true);
		outConfig.set("ExtraHardMode.EnderDragon.DropsVillagerEggs", this.config_enderDragonDropsVillagerEggs);
		
		this.config_enderDragonAdditionalAttacks = config.getBoolean("ExtraHardMode.EnderDragon.HarderBattle", true);
		outConfig.set("ExtraHardMode.EnderDragon.HarderBattle", this.config_enderDragonAdditionalAttacks);
		
		this.config_enderDragonCombatAnnouncements = config.getBoolean("ExtraHardMode.EnderDragon.BattleAnnouncements", true);
		outConfig.set("ExtraHardMode.EnderDragon.BattleAnnouncements", this.config_enderDragonCombatAnnouncements);
		
		this.config_enderDragonNoBuilding = config.getBoolean("ExtraHardMode.EnderDragon.NoBuildingAllowed", true);
		outConfig.set("ExtraHardMode.EnderDragon.NoBuildingAllowed", this.config_enderDragonNoBuilding);
		
		this.config_weakFoodCrops = config.getBoolean("ExtraHardMode.Farming.WeakCrops", true);
		outConfig.set("ExtraHardMode.Farming.WeakCrops", this.config_weakFoodCrops);
		
		this.config_noBonemealOnMushrooms = config.getBoolean("ExtraHardMode.Farming.NoBonemealOnMushrooms", true);
		outConfig.set("ExtraHardMode.Farming.NoBonemealOnMushrooms", this.config_noBonemealOnMushrooms);		
		
		this.config_noFarmingNetherWart = config.getBoolean("ExtraHardMode.Farming.NoFarmingNetherWart", true);
		outConfig.set("ExtraHardMode.Farming.NoFarmingNetherWart", this.config_noFarmingNetherWart);
		
		this.config_sheepRegrowWhiteWool = config.getBoolean("ExtraHardMode.Farming.SheepGrowOnlyWhiteWool", true);
		outConfig.set("ExtraHardMode.Farming.SheepGrowOnlyWhiteWool", this.config_sheepRegrowWhiteWool);
		
		this.config_dontMoveWaterSourceBlocks = config.getBoolean("ExtraHardMode.Farming.BucketsDontMoveWaterSources", true);
		outConfig.set("ExtraHardMode.Farming.BucketsDontMoveWaterSources", this.config_dontMoveWaterSourceBlocks);
		
		this.config_noSwimmingInArmor = config.getBoolean("ExtraHardMode.NoSwimmingWhenHeavy", true);
		outConfig.set("ExtraHardMode.NoSwimmingWhenHeavy", this.config_noSwimmingInArmor);		
		
		this.config_playerDeathItemStacksForfeitPercent = config.getInt("ExtraHardMode.PlayerDeath.ItemStacksForfeitPercent", 10);
		outConfig.set("ExtraHardMode.PlayerDeath.ItemStacksForfeitPercent", this.config_playerDeathItemStacksForfeitPercent);
		
		this.config_playerRespawnHealth = config.getInt("ExtraHardMode.PlayerDeath.RespawnHealth", 15);
		outConfig.set("ExtraHardMode.PlayerDeath.RespawnHealth", this.config_playerRespawnHealth);
		
		this.config_playerRespawnFoodLevel = config.getInt("ExtraHardMode.PlayerDeath.RespawnFoodLevel", 15);
		outConfig.set("ExtraHardMode.PlayerDeath.RespawnFoodLevel", this.config_playerRespawnFoodLevel);
		
		this.config_betterTreeChopping = config.getBoolean("ExtraHardMode.BetterTreeFelling", true);
		outConfig.set("ExtraHardMode.BetterTreeFelling", this.config_betterTreeChopping);		
		
		this.config_workAroundExplosionsBugs = config.getBoolean("ExtraHardMode.WorkAroundOtherPluginsExplosionBugs", false);
		outConfig.set("ExtraHardMode.WorkAroundOtherPluginsExplosionBugs", this.config_workAroundExplosionsBugs);
		
		
		//default additional falling blocks
		this.config_moreFallingBlocks = new ArrayList<Material>();
		this.config_moreFallingBlocks.add(Material.DIRT);
		this.config_moreFallingBlocks.add(Material.GRASS);
		this.config_moreFallingBlocks.add(Material.COBBLESTONE);
		this.config_moreFallingBlocks.add(Material.MOSSY_COBBLESTONE);
		this.config_moreFallingBlocks.add(Material.MYCEL);
		this.config_moreFallingBlocks.add(Material.JACK_O_LANTERN);
		
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
		outConfig.set("ExtraHardMode.AdditionalFallingBlocks", moreFallingBlocksList);
		
		//save config values to file system
		try
		{
			outConfig.save(DataStore.configFilePath);
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
		
		//FEATURE: monsters spawn in the light under a configurable Y level
		MoreMonstersTask task = new MoreMonstersTask();
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 1200L, 1200L);  //every 60 seconds
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
			//FEATURE: don't spam messages
			PlayerData playerData = ExtraHardMode.instance.dataStore.getPlayerData(player.getName());
			long now = Calendar.getInstance().getTimeInMillis();
			if(!message.equals(playerData.lastMessageSent) || now - playerData.lastMessageTimestamp > 30000)
			{
				player.sendMessage(color + message);
				playerData.lastMessageSent = message;
				playerData.lastMessageTimestamp = now;
			}
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
	
	boolean plantDies(Block block, byte newDataValue)
	{
		World world = block.getWorld();
		if(!ExtraHardMode.instance.config_enabled_worlds.contains(world) || !ExtraHardMode.instance.config_weakFoodCrops) return false;
		
		//not evaluated until the plant is nearly full grown
		if(newDataValue <= (byte)6) return false;
		
		Material material = block.getType();				
		if(material == Material.CROPS || material == Material.MELON_STEM || material == Material.CARROT || material == Material.PUMPKIN_STEM || material == Material.POTATO)
		{
			int deathProbability = 25;
			
			//plants in the dark always die
			if(block.getLightFromSky() < 10)
			{
				deathProbability = 100 ;
			}
			
			else
			{
			
				Biome biome = block.getBiome();
				
				//the desert environment is very rough on crops
				if(biome == Biome.DESERT || biome == Biome.DESERT_HILLS)
				{
					deathProbability += 50;
				}
				
				//unwatered crops are more likely to die
				Block belowBlock = block.getRelative(BlockFace.DOWN);
				byte moistureLevel = 0;
				if(belowBlock.getType() == Material.SOIL)
				{
					moistureLevel = belowBlock.getData();
				}
				
				if(moistureLevel == 0)
				{
					deathProbability += 25;
				}
			}
			
			if(ExtraHardMode.random(deathProbability))
			{
				return true;
			}			
		}
		
		return false;
	}
}