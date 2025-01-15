package io.github.alathra.alathraports.database;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.core.TravelNode;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.utility.DB;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.Record;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.github.alathra.alathraports.database.QueryUtils.*;
import static io.github.alathra.alathraports.database.schema.Tables.*;

/**
 * A class providing access to all SQL queries.
 */
@ApiStatus.Internal
public abstract class Queries {
    @ApiStatus.Internal
    public static abstract class Ports {
        /**
         * Used to delete a port
         *
         * @param port port
         * @return completable future
         */
        public static CompletableFuture<Void> deletePortQuery(final Port port) {
            final Port portClone = port.clone();

            return CompletableFuture.runAsync(() -> {
                try (
                    @NotNull Connection con = DB.getConnection()
                ) {
                    DSLContext context = DB.getContext(con);

                    context
                        .deleteFrom(PORTS)
                        .where(PORTS.IDENTIFIER.eq(UUIDUtil.toBytes(portClone.getUuid())))
                        .execute();
                } catch (SQLException e) {
                    Logger.get().error("SQL Query threw an error!", e);
                }
            });
        }

        /**
         * Used to fetch all ports
         *
         * @return list of ports
         */
        public static CompletableFuture<Set<Port>> loadAllPorts() {
            return CompletableFuture.supplyAsync(() -> {
                final Set<Port> ports = new HashSet<>();

                try (
                    Connection con = DB.getConnection()
                ) {
                    DSLContext context = DB.getContext(con);

                    Result<Record> result = context.select()
                        .from(PORTS)
                        .fetch();

                    for (Record record : result) {
                        var identifier = UUIDUtil.fromBytes(record.get(PORTS.IDENTIFIER));
                        var name = record.get(PORTS._NAME);
                        var size = record.get(PORTS.TRAVEL_NODE_SIZE);
                        var signIdentifier = UUIDUtil.fromBytes(record.get(PORTS.SIGN_WORLD_IDENTIFIER));
                        var signX = record.get(PORTS.SIGN_WORLD_X);
                        var signY = record.get(PORTS.SIGN_WORLD_Y);
                        var signZ = record.get(PORTS.SIGN_WORLD_Z);
                        var signPitch = record.get(PORTS.SIGN_WORLD_PITCH);
                        var signYaw = record.get(PORTS.SIGN_WORLD_YAW);
                        var teleportIdentifier = UUIDUtil.fromBytes(record.get(PORTS.TELEPORT_WORLD_IDENTIFIER));
                        var teleportX = record.get(PORTS.TELEPORT_WORLD_X);
                        var teleportY = record.get(PORTS.TELEPORT_WORLD_Y);
                        var teleportZ = record.get(PORTS.TELEPORT_WORLD_Z);
                        var teleportPitch = record.get(PORTS.TELEPORT_WORLD_PITCH);
                        var teleportYaw = record.get(PORTS.TELEPORT_WORLD_YAW);
                        var blockaded = record.get(PORTS.BLOCKADED);
                        var townIdentifier = UUIDUtil.fromBytes(record.get(PORTS.TOWN_IDENTIFIER));
                        var townFee = record.get(PORTS.TOWN_FEE);
                        var type = record.get(PORTS.TRAVEL_NODE_TYPE);

                        final World signWorld = Bukkit.getWorld(signIdentifier);
                        if (signWorld == null) {
                            Logger.get().warn(ColorParser.of("<yellow>Invalid world uuid for port <object>, skipping...")
                                .parseMinimessagePlaceholder("object", name)
                                .build()
                            );
                            continue;
                        }

                        final Location signLoc = new Location(signWorld, signX, signY, signZ, signYaw, signPitch);

                        final World teleportWorld = Bukkit.getWorld(teleportIdentifier);
                        if (teleportWorld == null) {
                            Logger.get().warn(ColorParser.of("<yellow>Invalid world uuid for port <object>, skipping...")
                                .parseMinimessagePlaceholder("object", name)
                                .build()
                            );
                            continue;
                        }

                        final Location teleportLoc = new Location(teleportWorld, teleportX, teleportY, teleportZ, teleportYaw, teleportPitch);

                        final var town = TownyAPI.getInstance().getTown(townIdentifier);

                        Port port = new Port(identifier, name, Settings.findPortSize(size), signLoc, teleportLoc);
                        if (town != null) {
                            port.setTown(town);
                        }
                        port.setBlockaded(BooleanUtil.fromByte(blockaded));
                        port.setTownFee(townFee);
                        ports.add(port);
                    }
                } catch (SQLException e) {
                    Logger.get().error("SQL Query threw an error!", e);
                }

                return ports;
            });
        }

        /**
         * Internally used in {@link #saveAllPorts(Set)} to generate the queries
         *
         * @param context context
         * @param port   port
         */
        private static void savePortsQueries(DSLContext context, final Port port) {
            final @Nullable UUID town = port.getTown() == null ? null : port.getTown().getUUID();
                context
                    .insertInto(
                        PORTS,
                        PORTS.IDENTIFIER,
                        PORTS._NAME,
                        PORTS.TRAVEL_NODE_SIZE,
                        PORTS.SIGN_WORLD_IDENTIFIER,
                        PORTS.SIGN_WORLD_X,
                        PORTS.SIGN_WORLD_Y,
                        PORTS.SIGN_WORLD_Z,
                        PORTS.SIGN_WORLD_PITCH,
                        PORTS.SIGN_WORLD_YAW,
                        PORTS.TELEPORT_WORLD_IDENTIFIER,
                        PORTS.TELEPORT_WORLD_X,
                        PORTS.TELEPORT_WORLD_Y,
                        PORTS.TELEPORT_WORLD_Z,
                        PORTS.TELEPORT_WORLD_PITCH,
                        PORTS.TELEPORT_WORLD_YAW,
                        PORTS.BLOCKADED,
                        PORTS.TOWN_IDENTIFIER,
                        PORTS.TOWN_FEE,
                        PORTS.TRAVEL_NODE_TYPE
                    )
                    .values(
                        UUIDUtil.toBytes(port.getUuid()),
                        port.getName(),
                        port.getSize().getTier(),
                        UUIDUtil.toBytes(port.getSignLocation().getWorld().getUID()),
                        port.getSignLocation().getX(),
                        port.getSignLocation().getY(),
                        port.getSignLocation().getZ(),
                        port.getSignLocation().getPitch(),
                        port.getSignLocation().getYaw(),
                        UUIDUtil.toBytes(port.getTeleportLocation().getWorld().getUID()),
                        port.getTeleportLocation().getX(),
                        port.getTeleportLocation().getY(),
                        port.getTeleportLocation().getZ(),
                        port.getTeleportLocation().getPitch(),
                        port.getTeleportLocation().getYaw(),
                        BooleanUtil.toByte(port.isBlockaded()),
                        UUIDUtil.toBytes(town),
                        port.getTownFee(),
                        port.getType().name()
                    )
                    .onDuplicateKeyUpdate()
                    .set(PORTS._NAME, port.getName())
                    .set(PORTS.TRAVEL_NODE_SIZE, port.getSize().getTier())
                    .set(PORTS.SIGN_WORLD_IDENTIFIER, UUIDUtil.toBytes(port.getSignLocation().getWorld().getUID()))
                    .set(PORTS.SIGN_WORLD_X, port.getSignLocation().getX())
                    .set(PORTS.SIGN_WORLD_Y, port.getSignLocation().getY())
                    .set(PORTS.SIGN_WORLD_Z, port.getSignLocation().getZ())
                    .set(PORTS.SIGN_WORLD_PITCH, port.getSignLocation().getPitch())
                    .set(PORTS.SIGN_WORLD_YAW, port.getSignLocation().getYaw())
                    .set(PORTS.TELEPORT_WORLD_IDENTIFIER, UUIDUtil.toBytes(port.getTeleportLocation().getWorld().getUID()))
                    .set(PORTS.TELEPORT_WORLD_X, port.getTeleportLocation().getX())
                    .set(PORTS.TELEPORT_WORLD_Y, port.getTeleportLocation().getY())
                    .set(PORTS.TELEPORT_WORLD_Z, port.getTeleportLocation().getZ())
                    .set(PORTS.TELEPORT_WORLD_PITCH, port.getTeleportLocation().getPitch())
                    .set(PORTS.TELEPORT_WORLD_YAW, port.getTeleportLocation().getYaw())
                    .set(PORTS.BLOCKADED, BooleanUtil.toByte(port.isBlockaded()))
                    .set(PORTS.TOWN_IDENTIFIER, UUIDUtil.toBytes(port.getTown().getUUID()))
                    .set(PORTS.TOWN_FEE, port.getTownFee())
                    .set(PORTS.TRAVEL_NODE_TYPE, port.getType().name())
                    .execute();
        }

        /**
         * Saves or updates the provided ports
         *
         * @param ports ports
         * @implNote The database queries are executed async
         */
        public static void saveAllPorts(final Set<Port> ports) {
            final Set<Port> portsClone = ports.stream()
                .map(Port::clone)
                .collect(Collectors.toUnmodifiableSet());

            CompletableFuture.runAsync(() -> {
                try (
                    @NotNull Connection con = DB.getConnection()
                ) {
                    DSLContext context = DB.getContext(con);

                    context
                        .batched((configuration) -> {
                            for (final Port port : portsClone)
                                savePortsQueries(configuration.dsl(), port);
                        });
                } catch (SQLException e) {
                    Logger.get().error("SQL Query threw an error!", e);
                }
            });
        }
    }

    @ApiStatus.Internal
    public static abstract class Carriages {
        /**
         * Used to delete a carriage station
         *
         * @param station carriage
         * @return completable future
         */
        public static CompletableFuture<Void> deleteCarriageQuery(final CarriageStation station) {
            final CarriageStation stationClone = station.clone();

            return CompletableFuture.runAsync(() -> {
                try (
                    @NotNull Connection con = DB.getConnection()
                ) {
                    DSLContext context = DB.getContext(con);

                    context
                        .batch(
                            context
                                .deleteFrom(CARRIAGESTATIONS)
                                .where(CARRIAGESTATIONS.IDENTIFIER.eq(UUIDUtil.toBytes(stationClone.getUuid()))),
                            context
                                .deleteFrom(CARRIAGESTATIONS_CONNECTIONS)
                                .where(CARRIAGESTATIONS_CONNECTIONS.CARRIAGE_STATION_IDENTIFIER.eq(UUIDUtil.toBytes(stationClone.getUuid())))
                                .or(CARRIAGESTATIONS_CONNECTIONS.CARRIAGE_STATION_TARGET_IDENTIFIER.eq(UUIDUtil.toBytes(stationClone.getUuid())))
                        )
                        .execute();
                } catch (SQLException e) {
                    Logger.get().error("SQL Query threw an error!", e);
                }
            });
        }

        private static Set<UUID> loadDirectConnections(DSLContext context, UUID carriageStationIdentifier) {
            Result<Record> result = context.select()
                .from(CARRIAGESTATIONS_CONNECTIONS)
                .where(CARRIAGESTATIONS_CONNECTIONS.CARRIAGE_STATION_IDENTIFIER.eq(UUIDUtil.toBytes(carriageStationIdentifier)))
                .fetch();

            final Set<UUID> directConnections = new HashSet<>();

            for (Record record : result) {
                directConnections.add(
                    UUIDUtil.fromBytes(record.get(CARRIAGESTATIONS_CONNECTIONS.CARRIAGE_STATION_TARGET_IDENTIFIER))
                );
            }

            return directConnections;
        }

        /**
         * Used to fetch all carriage stations
         *
         * @return list of carriage stations
         */
        public static CompletableFuture<Map<CarriageStation, Set<UUID>>> loadAllCarriages() {
            return CompletableFuture.supplyAsync(() -> {
                final Map<CarriageStation, Set<UUID>> carriages = new HashMap<>();

                try (
                    Connection con = DB.getConnection()
                ) {
                    DSLContext context = DB.getContext(con);

                    Result<Record> result = context.select()
                        .from(CARRIAGESTATIONS)
                        .fetch();

                    for (Record record : result) {
                        var identifier = UUIDUtil.fromBytes(record.get(CARRIAGESTATIONS.IDENTIFIER));
                        var name = record.get(CARRIAGESTATIONS._NAME);
                        var size = record.get(CARRIAGESTATIONS.TRAVEL_NODE_SIZE);
                        var signIdentifier = UUIDUtil.fromBytes(record.get(CARRIAGESTATIONS.SIGN_WORLD_IDENTIFIER));
                        var signX = record.get(CARRIAGESTATIONS.SIGN_WORLD_X);
                        var signY = record.get(CARRIAGESTATIONS.SIGN_WORLD_Y);
                        var signZ = record.get(CARRIAGESTATIONS.SIGN_WORLD_Z);
                        var signPitch = record.get(CARRIAGESTATIONS.SIGN_WORLD_PITCH);
                        var signYaw = record.get(CARRIAGESTATIONS.SIGN_WORLD_YAW);
                        var teleportIdentifier = UUIDUtil.fromBytes(record.get(CARRIAGESTATIONS.TELEPORT_WORLD_IDENTIFIER));
                        var teleportX = record.get(CARRIAGESTATIONS.TELEPORT_WORLD_X);
                        var teleportY = record.get(CARRIAGESTATIONS.TELEPORT_WORLD_Y);
                        var teleportZ = record.get(CARRIAGESTATIONS.TELEPORT_WORLD_Z);
                        var teleportPitch = record.get(CARRIAGESTATIONS.TELEPORT_WORLD_PITCH);
                        var teleportYaw = record.get(CARRIAGESTATIONS.TELEPORT_WORLD_YAW);
                        var blockaded = record.get(CARRIAGESTATIONS.BLOCKADED);
                        var townIdentifier = UUIDUtil.fromBytes(record.get(CARRIAGESTATIONS.TOWN_IDENTIFIER));
                        var townFee = record.get(CARRIAGESTATIONS.TOWN_FEE);
                        var type = record.get(CARRIAGESTATIONS.TRAVEL_NODE_TYPE);

                        final World signWorld = Bukkit.getWorld(signIdentifier);
                        if (signWorld == null) {
                            Logger.get().warn(ColorParser.of("<yellow>Invalid world uuid for carriage <object>, skipping...")
                                .parseMinimessagePlaceholder("object", name)
                                .build()
                            );
                            continue;
                        }

                        final Location signLoc = new Location(signWorld, signX, signY, signZ, signYaw, signPitch);

                        final World teleportWorld = Bukkit.getWorld(teleportIdentifier);
                        if (teleportWorld == null) {
                            Logger.get().warn(ColorParser.of("<yellow>Invalid world uuid for carriage <object>, skipping...")
                                .parseMinimessagePlaceholder("object", name)
                                .build()
                            );
                            continue;
                        }

                        final Location teleportLoc = new Location(teleportWorld, teleportX, teleportY, teleportZ, teleportYaw, teleportPitch);

                        final var town = TownyAPI.getInstance().getTown(townIdentifier);

                        CarriageStation carriageStation = new CarriageStation(identifier, name, Settings.findCarriageStationSize(size), signLoc, teleportLoc);
                        if (town != null) {
                            carriageStation.setTown(town);
                        }
                        carriageStation.setBlockaded(BooleanUtil.fromByte(blockaded));
                        carriageStation.setTownFee(townFee);

                        final Set<UUID> directConnections = loadDirectConnections(context, identifier);
                        carriages.put(carriageStation, directConnections);
                    }
                } catch (SQLException e) {
                    Logger.get().error("SQL Query threw an error!", e);
                }

                return carriages;
            });
        }

        /**
         * Internally used in {@link #saveAllCarriages(Set)} to generate the queries
         *
         * @param context   context
         * @param station carriage
         */
        private static void saveCarriagesQueries(DSLContext context, final CarriageStation station) {
            final @Nullable UUID town = station.getTown() == null ? null : station.getTown().getUUID();

            context
                .insertInto(
                    CARRIAGESTATIONS,
                    CARRIAGESTATIONS.IDENTIFIER,
                    CARRIAGESTATIONS._NAME,
                    CARRIAGESTATIONS.TRAVEL_NODE_SIZE,
                    CARRIAGESTATIONS.SIGN_WORLD_IDENTIFIER,
                    CARRIAGESTATIONS.SIGN_WORLD_X,
                    CARRIAGESTATIONS.SIGN_WORLD_Y,
                    CARRIAGESTATIONS.SIGN_WORLD_Z,
                    CARRIAGESTATIONS.SIGN_WORLD_PITCH,
                    CARRIAGESTATIONS.SIGN_WORLD_YAW,
                    CARRIAGESTATIONS.TELEPORT_WORLD_IDENTIFIER,
                    CARRIAGESTATIONS.TELEPORT_WORLD_X,
                    CARRIAGESTATIONS.TELEPORT_WORLD_Y,
                    CARRIAGESTATIONS.TELEPORT_WORLD_Z,
                    CARRIAGESTATIONS.TELEPORT_WORLD_PITCH,
                    CARRIAGESTATIONS.TELEPORT_WORLD_YAW,
                    CARRIAGESTATIONS.BLOCKADED,
                    CARRIAGESTATIONS.TOWN_IDENTIFIER,
                    CARRIAGESTATIONS.TOWN_FEE,
                    CARRIAGESTATIONS.TRAVEL_NODE_TYPE
                )
                .values(
                    UUIDUtil.toBytes(station.getUuid()),
                    station.getName(),
                    station.getSize().getTier(),
                    UUIDUtil.toBytes(station.getSignLocation().getWorld().getUID()),
                    station.getSignLocation().getX(),
                    station.getSignLocation().getY(),
                    station.getSignLocation().getZ(),
                    station.getSignLocation().getPitch(),
                    station.getSignLocation().getYaw(),
                    UUIDUtil.toBytes(station.getTeleportLocation().getWorld().getUID()),
                    station.getTeleportLocation().getX(),
                    station.getTeleportLocation().getY(),
                    station.getTeleportLocation().getZ(),
                    station.getTeleportLocation().getPitch(),
                    station.getTeleportLocation().getYaw(),
                    BooleanUtil.toByte(station.isBlockaded()),
                    UUIDUtil.toBytes(town),
                    station.getTownFee(),
                    station.getType().name()
                )
                .onDuplicateKeyUpdate()
                .set(CARRIAGESTATIONS._NAME, station.getName())
                .set(CARRIAGESTATIONS.TRAVEL_NODE_SIZE, station.getSize().getTier())
                .set(CARRIAGESTATIONS.SIGN_WORLD_IDENTIFIER, UUIDUtil.toBytes(station.getSignLocation().getWorld().getUID()))
                .set(CARRIAGESTATIONS.SIGN_WORLD_X, station.getSignLocation().getX())
                .set(CARRIAGESTATIONS.SIGN_WORLD_Y, station.getSignLocation().getY())
                .set(CARRIAGESTATIONS.SIGN_WORLD_Z, station.getSignLocation().getZ())
                .set(CARRIAGESTATIONS.SIGN_WORLD_PITCH, station.getSignLocation().getPitch())
                .set(CARRIAGESTATIONS.SIGN_WORLD_YAW, station.getSignLocation().getYaw())
                .set(CARRIAGESTATIONS.TELEPORT_WORLD_IDENTIFIER, UUIDUtil.toBytes(station.getTeleportLocation().getWorld().getUID()))
                .set(CARRIAGESTATIONS.TELEPORT_WORLD_X, station.getTeleportLocation().getX())
                .set(CARRIAGESTATIONS.TELEPORT_WORLD_Y, station.getTeleportLocation().getY())
                .set(CARRIAGESTATIONS.TELEPORT_WORLD_Z, station.getTeleportLocation().getZ())
                .set(CARRIAGESTATIONS.TELEPORT_WORLD_PITCH, station.getTeleportLocation().getPitch())
                .set(CARRIAGESTATIONS.TELEPORT_WORLD_YAW, station.getTeleportLocation().getYaw())
                .set(CARRIAGESTATIONS.BLOCKADED, BooleanUtil.toByte(station.isBlockaded()))
                .set(CARRIAGESTATIONS.TOWN_IDENTIFIER, UUIDUtil.toBytes(station.getTown().getUUID()))
                .set(CARRIAGESTATIONS.TOWN_FEE, station.getTownFee())
                .set(CARRIAGESTATIONS.TRAVEL_NODE_TYPE, station.getType().name())
                .execute();

            for (TravelNode targetStation : station.getDirectConnections()) {
                context
                    .deleteFrom(CARRIAGESTATIONS_CONNECTIONS)
                    .where(CARRIAGESTATIONS_CONNECTIONS.CARRIAGE_STATION_IDENTIFIER.eq(UUIDUtil.toBytes(station.getUuid())))
                    .execute();

                context
                    .insertInto(
                        CARRIAGESTATIONS_CONNECTIONS,
                        CARRIAGESTATIONS_CONNECTIONS.CARRIAGE_STATION_IDENTIFIER,
                        CARRIAGESTATIONS_CONNECTIONS.CARRIAGE_STATION_TARGET_IDENTIFIER
                    )
                    .values(
                        UUIDUtil.toBytes(station.getUuid()),
                        UUIDUtil.toBytes(targetStation.getUuid())
                    )
                    .execute();
            }
        }

        /**
         * Saves or updates the provided carriages
         *
         * @param carriageStations carriages
         * @implNote The database queries are executed async
         */
        public static void saveAllCarriages(final Set<CarriageStation> carriageStations) {
            final Set<CarriageStation> carriagesClone = carriageStations.stream()
                .map(CarriageStation::clone)
                .collect(Collectors.toUnmodifiableSet());

            CompletableFuture.runAsync(() -> {
                try (
                    @NotNull Connection con = DB.getConnection()
                ) {
                    DSLContext context = DB.getContext(con);

                    context.batched(configuration -> {
                        for (final CarriageStation carriageStation : carriagesClone)
                            saveCarriagesQueries(configuration.dsl(), carriageStation);
                    });
                } catch (SQLException e) {
                    Logger.get().error("SQL Query threw an error!", e);
                }
            });
        }
    }
}
