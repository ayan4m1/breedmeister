package me.ayan4m1.plugins.breedmeister;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BreedMeister extends JavaPlugin implements Listener {
	//Map of entity ids to timestamps representing when they may next breed
	private HashMap<Integer, Long> breedTimes = new HashMap<Integer, Long>();

	//Maximum distance from dispenser to animal in blocks
	private Integer maxDistance = 5;

	//Per-animal delay between breeding in minutes
	private Integer breedDelay = 5;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	public void onDisable() {
		this.breedTimes.clear();
	}

	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
		Block block = event.getBlock();

		//Only continue if wheat is being dropped from a dispenser
		if (block.getType() != Material.DISPENSER || event.getItem().getType() != Material.WHEAT) {
			return;
		}

		List<Entity> entities = block.getWorld().getEntities();

		//Search for two valid and distinct animals
		Animals animalOne = this.findValidAnimal(entities, block.getLocation(), null);
		if (animalOne == null) {
			this.getLogger().info("Found no animals");
			return;
		}

		Animals animalTwo = this.findValidAnimal(entities, block.getLocation(), animalOne);
		if (animalTwo == null) {
			this.getLogger().info("Found only one animal");
			return;
		}

		//Spawn a baby and cancel the BlockDispenseEvent
		Animals newAnimal = (Animals)block.getWorld().spawnCreature(animalTwo.getLocation().add(1, 0, 1), animalTwo.getType());
		newAnimal.setBaby();
		event.setCancelled(true);

		//Add the animals to the bred list
		Long nextBreedTime = (new Date().getTime() / 1000) + (this.breedDelay * 60);
		this.breedTimes.put(animalOne.getEntityId(), nextBreedTime);
		this.breedTimes.put(animalTwo.getEntityId(), nextBreedTime);
	}

	private Animals findValidAnimal(List<Entity> entities, Location dispenserLoc, Animals exceptThis) {
		for(Entity e : entities) {
			//Ensure entity is a living, adult animal
			if (!(e instanceof Animals && ((Animals)e).canBreed()) || e.isDead()) {
				continue;
			}

			//Ensure animal is within maxDistance blocks from the dispenser
			if (dispenserLoc.distance(e.getLocation()) > this.maxDistance) {
				continue;
			}

			//Ensure the animal can breed
			if (this.breedTimes.containsKey(e.getEntityId())) {
				if (this.breedTimes.get(e.getEntityId()) > (new Date().getTime() / 1000)) {
					continue;
				} else {
					this.breedTimes.remove(e.getEntityId());
				}
			}

			//Ensure the animal is not one we have already found
			if (exceptThis != null
					&& e.getType().equals(exceptThis.getType())
					&& exceptThis.getEntityId() != e.getEntityId()) {
				continue;
			}

			return (Animals)e;
		}
		return null;
	}
}
