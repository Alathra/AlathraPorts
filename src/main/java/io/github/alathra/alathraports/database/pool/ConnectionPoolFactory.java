package io.github.alathra.alathraports.database.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.alathra.alathraports.database.config.DatabaseConfig;
import io.github.alathra.alathraports.database.exception.DatabaseInitializationException;
import org.slf4j.Logger;

public abstract class ConnectionPoolFactory {
    public static HikariDataSource create(DatabaseConfig databaseConfig, Logger logger) throws DatabaseInitializationException {
        try {
            HikariConfig hikariConfig = ConnectionPoolConfigFactory.get(databaseConfig);

            return new HikariDataSource(hikariConfig);
        } catch (Throwable t) {
            throw new DatabaseInitializationException("Failed to initialize database pool during startup. Are you using the correct database type?");
        }
    }
}
