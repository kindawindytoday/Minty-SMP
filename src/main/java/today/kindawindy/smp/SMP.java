package today.kindawindy.smp;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import today.kindawindy.smp.database.MySQLConnector;
import today.kindawindy.smp.database.MySQLCreator;
import today.kindawindy.smp.manager.PlayerManager;
import today.kindawindy.smp.manager.TagManager;
import today.kindawindy.smp.registry.ConfigRegistry;
import today.kindawindy.smp.registry.DiscordRegistry;
import today.kindawindy.smp.registry.PlayerRegistry;

@Getter
public enum SMP {

    INSTANCE;

    private JavaPlugin plugin;
    private ConfigRegistry configRegistry;

    private MySQLConnector connector;
    private MySQLCreator creator;

    private PlayerRegistry playerRegistry;
    private DiscordRegistry discordRegistry;

    private PlayerManager playerManager;
    private TagManager tagManager;

    public void load(JavaPlugin plugin) {
        this.plugin = plugin;

        // for jooq (delete shit)
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
    }

    public void start(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configRegistry = new ConfigRegistry(this.plugin);

        this.connector = new MySQLConnector(this);
        this.creator = new MySQLCreator(this.connector);

        this.playerRegistry = new PlayerRegistry(this);
        this.discordRegistry = new DiscordRegistry(this);

        this.playerManager = new PlayerManager(this);
        this.tagManager = new TagManager(this);
    }

    public void stop(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerRegistry.saveAll();
        this.connector.shutdown();
        this.discordRegistry.getJda().shutdown();
    }
}
