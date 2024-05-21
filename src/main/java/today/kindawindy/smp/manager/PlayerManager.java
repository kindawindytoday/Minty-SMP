package today.kindawindy.smp.manager;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import today.kindawindy.smp.SMP;
import today.kindawindy.smp.objects.GamePlayer;
import today.kindawindy.smp.registry.PlayerRegistry;
import today.kindawindy.smp.util.ChatUtil;

public class PlayerManager implements Listener {

    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final PlayerRegistry playerRegistry;
    private final Guild guild;

    public PlayerManager(SMP instance) {
        this.plugin = instance.getPlugin();
        this.config = plugin.getConfig();
        this.playerRegistry = instance.getPlayerRegistry();
        this.guild = instance.getDiscordRegistry().getGuild();

        init();
    }

    private void init() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = playerRegistry.getOrLoad(player.getName());
        Member member = guild.getMemberById(gamePlayer.getDiscordId());

        if (member == null)
            return;

        Role highRole = member.getRoles().get(0);
        String hexColor = "&#" + Integer.toHexString(highRole.getColor().getRGB())
                .substring(2);
        String prefix = String.format("&7[%s%s&7]", hexColor, highRole.getName());

        gamePlayer.setPrefix(prefix);
        gamePlayer.setPriority(highRole.getPosition());
    }

    @EventHandler
    public void handle(AsyncChatEvent event) {
        event.setCancelled(true);

        TextChannel channel = guild.getTextChannelById(config.getLong("discord.id.chat"));

        if (channel == null)
            return;

        Player player = event.getPlayer();
        GamePlayer gamePlayer = playerRegistry.getOrLoad(player.getName());
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        Object[] args = {gamePlayer.getPrefix(), player.getName(), message};

        channel.sendMessage(ChatUtil.getFormat(false, args)).queue();
        Bukkit.broadcastMessage(ChatUtil.getFormat(args));
    }

    @EventHandler
    public void handle(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        String ip = event.getAddress().getHostAddress();
        GamePlayer gamePlayer = playerRegistry.getOrLoad(name);

        if (!gamePlayer.getIp().isEmpty() && !gamePlayer.getIp().equals(ip)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    """
                            §4A suspicious attempt to log in to the server was detected.
                                                        
                            §7If you are the account owner, then reset the session.
                            §7Use the "/reset" command to reset.
                            """);
            return;
        }

        gamePlayer.setIp(ip);

        if (gamePlayer.getDiscordId() != 0)
            return;

        String code = RandomStringUtils.randomAlphanumeric(4, 8);

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                "§7To register on the server, you need to verify your account.\n\n" +
                        "§7Use the \"/verify " + code + "\" command to verify.");
        gamePlayer.setCode(code);
    }
}
