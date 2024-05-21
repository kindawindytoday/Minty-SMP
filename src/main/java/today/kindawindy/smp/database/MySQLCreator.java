package today.kindawindy.smp.database;

public class MySQLCreator {

    public MySQLCreator(MySQLConnector connector) {
        init(connector);
    }

    private void init(MySQLConnector connector) {
        SQL.sync("CREATE TABLE IF NOT EXISTS `" + connector.prefix() + "players` (\n" +
                "   `name` VARCHAR(16) NOT NULL,\n" +
                "   `data` JSON NOT NULL,\n" +
                "   `discordId` BIGINT(20) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(`data`, '$.discordId'))),\n" +
                "   PRIMARY KEY (`name`)\n" +
                ");");
    }
}