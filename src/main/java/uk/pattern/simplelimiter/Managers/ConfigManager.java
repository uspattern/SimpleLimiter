package uk.pattern.simplelimiter.Managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigManager {

    private final JavaPlugin plugin;

    private int mobLimit = -1;
    private int vehicleLimit = -1;
    private final Map<EntityType, Integer> specificLimits = new HashMap<>();
    private final Set<EntityType> ignoredEntities = new HashSet<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadLimits() {
        specificLimits.clear();
        ignoredEntities.clear();

        FileConfiguration config = plugin.getConfig();

        if (config.isList("ignored")) {
            for (String ignoredName : config.getStringList("ignored")) {
                try {
                    EntityType ignoredType = EntityType.valueOf(ignoredName.toUpperCase());
                    ignoredEntities.add(ignoredType);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Unknown EntityType in ignored list: " + ignoredName);
                }
            }
        }

        if (config.isConfigurationSection("limits")) {
            var limitsSection = config.getConfigurationSection("limits");
            if (limitsSection != null) {
                for (String key : limitsSection.getKeys(false)) {
                    String keyUpper = key.toUpperCase();
                    switch (keyUpper) {
                        case "MOB":
                            mobLimit = config.getInt("limits." + key, -1);
                            break;
                        case "VEHICLE":
                            vehicleLimit = config.getInt("limits." + key, -1);
                            break;
                        default:
                            try {
                                EntityType type = EntityType.valueOf(keyUpper);
                                int limit = config.getInt("limits." + key);
                                specificLimits.put(type, limit);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Unknown EntityType or limit key in config: " + key);
                            }
                            break;
                    }
                }
            }
        }
    }

    public int getMobLimit() {
        return mobLimit;
    }

    public int getVehicleLimit() {
        return vehicleLimit;
    }

    public Map<EntityType, Integer> getSpecificLimits() {
        return specificLimits;
    }

    public Set<EntityType> getIgnoredEntities() {
        return ignoredEntities;
    }

    public int getCheckRadius() {
        return plugin.getConfig().getInt("check-radius", 16); // 16 — значение по умолчанию
    }
}