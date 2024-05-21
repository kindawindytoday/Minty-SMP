package today.kindawindy.smp.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import today.kindawindy.smp.SMP;

import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
@Accessors(fluent = true)
public class MySQLConnector {

    private final String host;
    private final String username;
    private final String password;
    private final String database;
    private final String prefix;

    private DSLContext create;
    private HikariDataSource dataSource;

    private final JavaPlugin plugin;
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    public MySQLConnector(SMP instance) {
        this.plugin = instance.getPlugin();
        FileConfiguration config = this.plugin.getConfig();
        this.host = config.getString("mysql.host") + ":" + config.getInt("mysql.port");
        this.username = config.getString("mysql.username");
        this.password = config.getString("mysql.password");
        this.database = config.getString("mysql.database");
        this.prefix = config.getString("mysql.prefix");

        connect();
        startCheckScheduler();
    }

    private void connect() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + this.host + "/" + this.database);
        config.setUsername(this.username);
        config.setPassword(this.password);
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.addDataSourceProperty("useSSL", false);

        //Performance
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("maintainTimeStats", false);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("useServerPrepStmts", true);
        //Encoding (utf8 or utf8mb4)
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("characterSetResults", "utf8");
        //Miscellaneous
        config.addDataSourceProperty("serverTimezone", TimeZone.getDefault().getID());
        config.addDataSourceProperty("useJDBCCompliantTimezoneShift", true);
        config.addDataSourceProperty("useLegacyDatetimeCode", true);

        try {
            dataSource = new HikariDataSource(config);
            create = DSL.using(dataSource, SQLDialect.MYSQL);
        } catch (Exception ex) {
            throw new RuntimeException("СОЕДИНЕНИЕ С БД НЕ УСТАНОВЛЕНО! ПИЗДЕЦ!", ex);
        }
    }

    private void startCheckScheduler() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if (!service.isShutdown() && dataSource.isClosed()) {
                plugin.getLogger().info("Соединение с БД пропало! Восстанавливаем..");
                connect();

                if (dataSource.isClosed())
                    plugin.getLogger().severe("Соединение восстановить не удалось. Пиздец всему пизда!");
                else
                    plugin.getLogger().info("Соединение восстановлено. Можно жить спокойно.");
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    @SneakyThrows
    public void shutdown() {
        plugin.getLogger().info("Выполняем оставшиеся запросы..");
        service.shutdown();

        if (!service.awaitTermination(15, TimeUnit.SECONDS))
            plugin.getLogger().severe("Не удалось выполнить оставшиеся запросы =(");

        plugin.getLogger().info("Отключение сессии с базой данных..");
        dataSource.close();
    }
}