package io.github.alathra.alathraports.database;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.database.schema.tables.records.CarriagestationsRecord;
import io.github.alathra.alathraports.database.schema.tables.records.PortsRecord;
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
        public static Set<Port> loadAllPorts() {
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
                    var abstracted = record.get(PORTS.ABSTRACTED);
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

                    Town town = null;
                    if (townIdentifier != null)
                        town = TownyAPI.getInstance().getTown(townIdentifier);

                    Port port = new Port(identifier, name, Settings.findPortSize(size), signLoc, teleportLoc);

                    if (town != null)
                        port.setTown(town);

                    port.setBlockaded(BooleanUtil.fromByte(blockaded));
                    port.setAbstract(BooleanUtil.fromByte(abstracted));
                    port.setTownFee(townFee);
                    ports.add(port);
                }
            } catch (SQLException e) {
                Logger.get().error("SQL Query threw an error!", e);
            }
            return ports;
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

                    final List<PortsRecord> portsRecords = new ArrayList<>();

                    portsClone.forEach(port -> {
                        final @Nullable UUID town = port.getTown() == null ? null : port.getTown().getUUID();
                        portsRecords.add(new PortsRecord(
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
                            BooleanUtil.toByte(port.isAbstract()),
                            UUIDUtil.toBytes(town),
                            port.getTownFee(),
                            port.getType().name()
                        ));
                    });

                    context.batchMerge(portsRecords).execute();

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
            final CarriageStation carriageStationClone = station.clone();

            return CompletableFuture.runAsync(() -> {
                try (
                    @NotNull Connection con = DB.getConnection()
                ) {
                    DSLContext context = DB.getContext(con);

                    context
                        .deleteFrom(CARRIAGESTATIONS)
                        .where(CARRIAGESTATIONS.IDENTIFIER.eq(UUIDUtil.toBytes(carriageStationClone.getUuid())))
                        .execute();
                } catch (SQLException e) {
                    Logger.get().error("SQL Query threw an error!", e);
                }
            });
        }

        /**
         * Used to fetch all carriage stations
         *
         * @return list of carriage stations
         */
        public static Set<CarriageStation> loadAllCarriageStations() {
            final Set<CarriageStation> carriageStations = new HashSet<>();

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
                    var abstracted = record.get(CARRIAGESTATIONS.ABSTRACTED);
                    var townIdentifier = UUIDUtil.fromBytes(record.get(CARRIAGESTATIONS.TOWN_IDENTIFIER));
                    var townFee = record.get(CARRIAGESTATIONS.TOWN_FEE);
                    var type = record.get(CARRIAGESTATIONS.TRAVEL_NODE_TYPE);

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

                    Town town = null;
                    if (townIdentifier != null)
                        town = TownyAPI.getInstance().getTown(townIdentifier);

                    CarriageStation carriageStation = new CarriageStation(identifier, name, Settings.findCarriageStationSize(size), signLoc, teleportLoc);

                    if (town != null)
                        carriageStation.setTown(town);

                    carriageStation.setBlockaded(BooleanUtil.fromByte(blockaded));
                    carriageStation.setAbstract(BooleanUtil.fromByte(abstracted));
                    carriageStation.setTownFee(townFee);
                    carriageStations.add(carriageStation);
                }
            } catch (SQLException e) {
                Logger.get().error("SQL Query threw an error!", e);
            }
            return carriageStations;
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


                    final List<CarriagestationsRecord> carriageStationsRecords = new ArrayList<>();

                    carriagesClone.forEach(station -> {
                        final @Nullable UUID town = station.getTown() == null ? null : station.getTown().getUUID();
                        carriageStationsRecords.add(new CarriagestationsRecord(
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
                            BooleanUtil.toByte(station.isAbstract()),
                            UUIDUtil.toBytes(town),
                            station.getTownFee(),
                            station.getType().name()
                        ));
                    });

                    context.batchMerge(carriageStationsRecords).execute();

                } catch (SQLException e) {
                    Logger.get().error("SQL Query threw an error!", e);
                }
            });
        }
    }
}
