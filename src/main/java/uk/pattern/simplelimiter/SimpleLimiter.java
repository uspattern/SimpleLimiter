package uk.pattern.simplelimiter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import uk.pattern.simplelimiter.Main.LimitEnforcer;
import uk.pattern.simplelimiter.Managers.ConfigManager;

public class SimpleLimiter extends JavaPlugin implements Listener {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.loadLimits();

        LimitEnforcer limitEnforcer = new LimitEnforcer(configManager);

        Bukkit.getPluginManager().registerEvents(limitEnforcer, this);
        getLogger().info("SimpleSpawnLimiter enabled.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("slreload")) {
            reloadConfig();
            configManager.loadLimits();
            sender.sendMessage("§a> SimpleLimiter перезагружен!");
            return true;
        }
        return false;
    }
}