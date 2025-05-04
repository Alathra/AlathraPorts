CREATE TABLE IF NOT EXISTS "${tablePrefix}ports" (
    "identifier" BINARY(16) NOT NULL,
    "_name" TEXT,
    "travel_node_size" INT NOT NULL,
    "sign_world_identifier" BINARY(16),
    "sign_world_x" DOUBLE PRECISION,
    "sign_world_y" DOUBLE PRECISION,
    "sign_world_z" DOUBLE PRECISION,
    "sign_world_pitch" REAL,
    "sign_world_yaw" REAL,
    "teleport_world_identifier" BINARY(16),
    "teleport_world_x" DOUBLE PRECISION,
    "teleport_world_y" DOUBLE PRECISION,
    "teleport_world_z" DOUBLE PRECISION,
    "teleport_world_pitch" REAL,
    "teleport_world_yaw" REAL,
    "blockaded" TINYINT(1) NOT NULL,
    "abstracted" TINYINT(1) NOT NULL,
    "town_identifier" BINARY(16),
    "town_fee" DOUBLE PRECISION,
    "travel_node_type" TINYTEXT NOT NULL,
    PRIMARY KEY ("identifier")
);

CREATE TABLE IF NOT EXISTS "${tablePrefix}carriagestations" (
    "identifier" BINARY(16) NOT NULL,
    "_name" TEXT,
    "travel_node_size" INT NOT NULL,
    "sign_world_identifier" BINARY(16),
    "sign_world_x" DOUBLE PRECISION,
    "sign_world_y" DOUBLE PRECISION,
    "sign_world_z" DOUBLE PRECISION,
    "sign_world_pitch" REAL,
    "sign_world_yaw" REAL,
    "teleport_world_identifier" BINARY(16),
    "teleport_world_x" DOUBLE PRECISION,
    "teleport_world_y" DOUBLE PRECISION,
    "teleport_world_z" DOUBLE PRECISION,
    "teleport_world_pitch" REAL,
    "teleport_world_yaw" REAL,
    "blockaded" TINYINT(1) NOT NULL,
    "abstracted" TINYINT(1) NOT NULL,
    "town_identifier" BINARY(16),
    "town_fee" DOUBLE PRECISION,
    "travel_node_type" TINYTEXT NOT NULL,
    PRIMARY KEY ("identifier")
);
CREATE UNIQUE INDEX "${tablePrefix}carriagestations_identifier" ON "${tablePrefix}carriagestations" ("identifier"); -- Indexes and Unique indexed must be created in separate statements due to SQLite

CREATE TABLE IF NOT EXISTS "${tablePrefix}carriagestations_connections" (
    "carriage_station_identifier" BINARY(16) NOT NULL,
    "carriage_station_target_identifier" BINARY(16) NOT NULL
);