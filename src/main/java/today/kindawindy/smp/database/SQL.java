package today.kindawindy.smp.database;

import org.bukkit.Bukkit;
import org.jooq.DSLContext;
import today.kindawindy.smp.SMP;

import java.sql.SQLException;
import java.util.concurrent.Future;

public class SQL {

    private static final SMP instance = SMP.INSTANCE;
    private static final MySQLConnector connector = instance.getConnector();

    public static void async(String query, Object... bindings) {
        async(create -> create.execute(query, bindings));
    }

    public static Future<?> async(SQLConsumer create) {
        return connector.service().submit(() -> printTime(create, () -> {
            try {
                create.run(connector.create());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public static void sync(String sql, Object... bindings) {
        printTime(sql, () -> connector.create().execute(sql, bindings));
    }

    private static void printTime(Object print, Runnable runnable) {
        long start = System.currentTimeMillis();

        try {
            runnable.run();
        } finally {
            long after = System.currentTimeMillis() - start;

            if (after > 2000)
                Bukkit.getLogger().warning("SQL задержка, объект " + print + " выполнялся " + after + "ms..");
        }
    }

    public interface SQLConsumer {

        void run(DSLContext create) throws SQLException;
    }

    public interface SQLFunction<R> {

        R apply(DSLContext create) throws SQLException;
    }
}