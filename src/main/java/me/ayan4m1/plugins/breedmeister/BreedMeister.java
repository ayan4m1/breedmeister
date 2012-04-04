package me.ayan4m1.plugins.breedmeister;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BreedMeister extends JavaPlugin implements Listener {
    public void onDisable() {
        //TODO: No disable code needed for now
    }

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
    	if (event.getBlock().getType() == Material.DISPENSER) {
    		if (event.getItem().getType() == Material.WHEAT) {
    			List<Entity> entities = event.getBlock().getWorld().getEntities();
    			for (Entity e : entities) {
    				if (!(e instanceof Animals && ((Animals)e).canBreed()) || e.isDead()) {
    	    			continue;
    	    		}

    	    		if (event.getBlock().getLocation().distance(e.getLocation()) > 5) {
    	    			continue;
    	    		}

    	    		Animals newAnimal = (Animals)event.getBlock().getWorld().spawnCreature(e.getLocation().add(1, 0, 1), e.getType());
    	    		newAnimal.setBaby();
    	    		event.setCancelled(true);
    	    		break;
    			}
    		}
    	}
    }
}

