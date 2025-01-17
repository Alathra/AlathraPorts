package io.github.alathra.alathraports.database;

import io.github.alathra.alathraports.database.handler.DatabaseType;

record DatabaseTestParams(String jdbcPrefix, DatabaseType requiredDatabaseType, String tablePrefix) {
}
