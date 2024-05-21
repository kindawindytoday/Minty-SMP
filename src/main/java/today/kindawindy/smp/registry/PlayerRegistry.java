package today.kindawindy.smp.registry;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jooq.Record;
import today.kindawindy.smp.SMP;
import today.kindawindy.smp.database.MySQLConnector;
import today.kindawindy.smp.database.SQL;
import today.kindawindy.smp.objects.GamePlayer;
import today.kindawindy.smp.util.GsonUtil;

import java.util.Set;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class PlayerRegistry {

    private final MySQLConnector connector;

    @Getter
    private final Set<GamePlayer> players = Sets.newConcurrentHashSet();

    public PlayerRegistry(SMP instance) {
        this.connector = instance.getConnector();

        Bukkit.getScheduler().runTaskTimerAsynchronously(instance.getPlugin(),
                this::saveAll, 20L * 60L, 20L * 60L);
    }

    public GamePlayer getByDiscordId(long discordId) {
        GamePlayer gamePlayer = players.stream()
                .filter(player -> player.getDiscordId() == discordId)
                .findAny()
                .orElse(null);

        if (gamePlayer == null) {
            Record record = connector.create()
                    .selectFrom(table(connector.prefix() + "players"))
                    .where(field("discordId").equal(discordId))
                    .fetchOne();

            if (record != null) {
                String data = record.get(1, String.class);
                gamePlayer = GsonUtil.from(data, GamePlayer.class);
            }
        }
        return gamePlayer;
    }

    public GamePlayer getByCode(String code) {
        return players.stream()
                .filter(player -> player.getCode().equals(code))
                .findAny()
                .orElse(null);
    }

    public GamePlayer getOrLoad(String name) {
        GamePlayer gamePlayer = players.stream()
                .filter(player -> player.getName().equalsIgnoreCase(name))
                .findAny()
                .orElse(null);

        if (gamePlayer == null) {
            Record record = connector.create()
                    .selectFrom(table(connector.prefix() + "players"))
                    .where(field("name").equal(name))
                    .fetchOne();
            gamePlayer = new GamePlayer(name);

            if (record != null) {
                String data = record.get(1, String.class);
                gamePlayer = GsonUtil.from(data, GamePlayer.class);
            } else {
                String data = GsonUtil.to(gamePlayer);

                SQL.async(create -> create.insertInto(table(connector.prefix() + "players"))
                        .set(field("name"), name)
                        .set(field("data"), data)
                        .execute());
            }

            players.add(gamePlayer);
        }

        return gamePlayer;
    }

    public void saveAll() {
        SQL.async(create -> create.batched(c ->
                players.forEach(gamePlayer -> {
                    c.dsl().update(table(connector.prefix() + "players"))
                            .set(field("name"), gamePlayer.getName())
                            .set(field("data"), GsonUtil.to(gamePlayer))
                            .where(field("name").equal(gamePlayer.getName()))
                            .execute();
                })));
    }
}
