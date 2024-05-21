package today.kindawindy.smp.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
@Setter
@AllArgsConstructor
public class GamePlayer {

    private final String name;
    private String code;
    private long discordId;
    private String ip;
    private String prefix;
    private int priority;

    public GamePlayer(String name) {
        this.name = name;
        this.code = "";
        this.discordId = 0;
        this.ip = "";
        this.prefix = "";
        this.priority = 0;
    }

    public Player getPlayer() {
        return Bukkit.getPlayerExact(name);
    }
}
