package uk.pattern.simplelimiter.Main;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import uk.pattern.simplelimiter.Managers.ConfigManager;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class LimitEnforcer implements Listener {

    private final ConfigManager configManager;

    public LimitEnforcer(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        enforceLimits(event.getEntity());
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        enforceLimits(event.getEntity());
    }

    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent event) {
        enforceLimits(event.getVehicle());
    }

    private void enforceLimits(Entity entity) {
        EntityType type = entity.getType();
        Set<EntityType> ignoredEntities = configManager.getIgnoredEntities();
        if (ignoredEntities.contains(type)) return;

        int radius = configManager.getCheckRadius();

        // Все сущности рядом
        Entity[] nearby = entity.getWorld().getNearbyEntities(entity.getLocation(), radius, radius, radius)
                .toArray(new Entity[0]);

        Map<EntityType, Integer> specificLimits = configManager.getSpecificLimits();
        if (specificLimits.containsKey(type)) {
            int limit = specificLimits.get(type);
            long count = Arrays.stream(nearby)
                    .filter(e -> e.getType() == type)
                    .count();

            if (count >= limit) {
                for (Entity e : nearby) {
                    if (e.getType() == type && e.getEntityId() != entity.getEntityId()) {
                        e.remove();
                        break;
                    }
                }
            }
            return;
        }

        int mobLimit = configManager.getMobLimit();
        if (mobLimit > -1 && entity instanceof LivingEntity && !(entity instanceof Player) && !(entity instanceof Vehicle)) {
            long mobCount = Arrays.stream(nearby)
                    .filter(e -> e instanceof LivingEntity && !(e instanceof Player) && !(e instanceof Vehicle))
                    .count();

            if (mobCount >= mobLimit) {
                for (Entity e : nearby) {
                    if (e instanceof LivingEntity && !(e instanceof Player) && !(e instanceof Vehicle)
                            && e.getEntityId() != entity.getEntityId()) {
                        e.remove();
                        break;
                    }
                }
            }
            return;
        }

        int vehicleLimit = configManager.getVehicleLimit();
        if (vehicleLimit > -1 && entity instanceof Vehicle) {
            long vehicleCount = Arrays.stream(nearby)
                    .filter(e -> e instanceof Vehicle)
                    .count();

            if (vehicleCount >= vehicleLimit) {
                for (Entity e : nearby) {
                    if (e instanceof Vehicle && e.getEntityId() != entity.getEntityId()) {
                        e.remove();
                        break;
                    }
                }
            }
        }
    }
}

