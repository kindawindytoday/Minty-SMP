package today.kindawindy.smp.registry;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigRegistry {

    public ConfigRegistry(JavaPlugin plugin) {
        init(plugin);
    }

    private void init(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
    }
}
