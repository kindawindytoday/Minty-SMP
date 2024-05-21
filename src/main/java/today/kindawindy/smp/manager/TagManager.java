package today.kindawindy.smp.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import today.kindawindy.smp.SMP;
import today.kindawindy.smp.objects.GamePlayer;
import today.kindawindy.smp.registry.PlayerRegistry;
import today.kindawindy.smp.util.ChatUtil;

public class TagManager {

    private final JavaPlugin plugin;
    private final PlayerRegistry playerRegistry;

    public TagManager(SMP instance) {
        this.plugin = instance.getPlugin();
        this.playerRegistry = instance.getPlayerRegistry();

        init();
    }

    private void init() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () ->
                playerRegistry.getPlayers().forEach(this::show), 20 * 3, 20 * 3);
    }

    private void show(GamePlayer gamePlayer) {
        if (gamePlayer.getPlayer() == null)
            return;

        Scoreboard board = gamePlayer.getPlayer().getScoreboard();

        for (Player online : Bukkit.getOnlinePlayers()) {
            GamePlayer gameOnline = playerRegistry.getOrLoad(online.getName());
            String id = String.valueOf(1000 - gameOnline.getPriority());
            int length = online.getName().length() + id.length() >= 16 ?
                    online.getName().length() - id.length() : online.getName().length();
            id += online.getName().substring(0, length);
            Team team = board.getTeam(id);

            if (team == null)
                team = board.registerNewTeam(id);

            team.setPrefix(ChatUtil.colored("%s ", gameOnline.getPrefix()));
            team.addPlayer(online);
        }

        gamePlayer.getPlayer().setScoreboard(board);
    }
}
