package uk.pattern.simplelimiter;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
// Comment for me
public class SimpleLimiter extends JavaPlugin implements Listener {

    private int mobLimit = -1;
    private int vehicleLimit = -1;
    private final Map<EntityType, Integer> specificLimits = new HashMap<>();
    private final Set<EntityType> ignoredEntities = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadLimits();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("SimpleSpawnLimiter enabled.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("slreload")) {
            reloadConfig();
            loadLimits();
            sender.sendMessage("§a> SimpleLimiter перезагружен!");
            return true;
        }
        return false;
    }

    private void loadLimits() {
        specificLimits.clear();
        ignoredEntities.clear();

        FileConfiguration config = getConfig();

        if (config.isList("ignored")) {
            for (var ignoredName : config.getStringList("ignored")) {
                try {
                    var ignoredType = EntityType.valueOf(ignoredName.toUpperCase());
                    ignoredEntities.add(ignoredType);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Unknown EntityType in ignored list: " + ignoredName);
                }
            }
        }

        if (config.isConfigurationSection("limits")) {
            var limitsSection = config.getConfigurationSection("limits");
            if (limitsSection != null) {
                var keys = limitsSection.getKeys(false);
                for (var key : keys) {
                    var keyUpper = key.toUpperCase();

                    switch (keyUpper) {
                        case "MOB":
                            mobLimit = config.getInt("limits." + key, -1);
                            break;
                        case "VEHICLE":
                            vehicleLimit = config.getInt("limits." + key, -1);
                            break;
                        default:
                            try {
                                var type = EntityType.valueOf(keyUpper);
                                var limit = config.getInt("limits." + key);
                                specificLimits.put(type, limit);
                            } catch (IllegalArgumentException e) {
                                getLogger().warning("Unknown EntityType or limit key in config: " + key);
                            }
                            break;
                    }
                }
            }
        }
    }

    private void enforceLimits(Entity entity) {
        EntityType type = entity.getType();

        if (ignoredEntities.contains(type)) return;

        Chunk chunk = entity.getLocation().getChunk();

        // Проверка и удаление для конкретного типа из specificLimits
        if (specificLimits.containsKey(type)) {
            int limit = specificLimits.get(type);
            long count = Arrays.stream(chunk.getEntities())
                    .filter(e -> e.getType() == type)
                    .count();
            if (count > limit) {
                removeAnyEntityOfType(chunk, type);
            }
            return;
        }

        // Общий лимит для мобов (живых сущностей кроме игроков и транспорта)
        if (mobLimit > -1 && entity instanceof LivingEntity && !(entity instanceof Player) && !(entity instanceof Vehicle)) {
            long mobCount = Arrays.stream(chunk.getEntities())
                    .filter(e -> e instanceof LivingEntity && !(e instanceof Player) && !(e instanceof Vehicle))
                    .count();
            if (mobCount > mobLimit) {
                removeAnyMob(chunk);
            }
            return;
        }

        // Общий лимит для транспорта
        if (vehicleLimit > -1 && entity instanceof Vehicle) {
            long vehicleCount = Arrays.stream(chunk.getEntities())
                    .filter(e -> e instanceof Vehicle)
                    .count();
            if (vehicleCount > vehicleLimit) {
                removeAnyVehicle(chunk);
            }
        }
    }

    private void removeAnyEntityOfType(Chunk chunk, EntityType type) {
        for (Entity e : chunk.getEntities()) {
            if (e.getType() == type) {
                e.remove();
                return;
            }
        }
    }

    private void removeAnyMob(Chunk chunk) {
        for (Entity e : chunk.getEntities()) {
            if (e instanceof LivingEntity && !(e instanceof Player) && !(e instanceof Vehicle)) {
                e.remove();
                return;
            }
        }
    }

    private void removeAnyVehicle(Chunk chunk) {
        for (Entity e : chunk.getEntities()) {
            if (e instanceof Vehicle) {
                e.remove();
                return;
            }
        }
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
}
