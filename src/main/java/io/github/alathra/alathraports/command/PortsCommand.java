package io.github.alathra.alathraports.command;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.core.TravelNode;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.carriagestations.CarriageStationSize;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.core.ports.PortSize;
import io.github.alathra.alathraports.core.TravelNodesManager;
import io.github.alathra.alathraports.core.exceptions.TravelNodeRegisterException;
import io.github.alathra.alathraports.database.DBAction;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class PortsCommand {
    public static final String ADMIN_PERM = "alathraports.admin";

    protected PortsCommand() {
        new CommandAPICommand("ports")
            .withFullDescription("All port interactions")
            .withShortDescription("All port interactions")
            .withSubcommands(
                createCommand(),
                deleteCommand(),
                listCommand(),
                editCommand(),
                moveCommand(),
                teleportCommand(),
                reloadCommand(),
                blockade(),
                connect(),
                disconnect(),
                listConnections()
            )
            .executesPlayer(this::helpCommand)
            .register();
    }

    private void helpCommand(CommandSender sender, CommandArguments args) {
    }

    public CommandAPICommand createCommand() {
        return new CommandAPICommand("create")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
                new CommandAPICommand("port")
                    .withArguments(
                        new StringArgument("name")
                            .replaceSuggestions(ArgumentSuggestions.strings("Port_Name")),
                        CommandUtil.portSizeArgument("size")
                    ).executesPlayer((Player sender, CommandArguments args) -> {
                        PortSize portSize = (PortSize) args.get("size");
                        String nodeName = (String) args.get("name");
                        RayTraceResult rayTrace = sender.rayTraceBlocks(5, FluidCollisionMode.NEVER);
                        if (rayTrace != null) {
                            Block block = rayTrace.getHitBlock();
                            BlockFace blockFace = rayTrace.getHitBlockFace();
                            if (block == null) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, no base block").build());
                            }
                            if (blockFace == null) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, no base block face").build());
                            }
                            if (!(block.isSolid()) || !(block.isCollidable())) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, base block is not solid").build());
                            }
                            switch (blockFace) {
                                case UP, DOWN:
                                    if(block.getRelative(BlockFace.UP).isEmpty()) {
                                        Location signLocation = block.getRelative(BlockFace.UP).getLocation();
                                        Port port = new Port(nodeName, portSize, signLocation, signLocation);
                                        if (AlathraPorts.getTownyHook().isTownyLoaded()) {
                                            port.findTown();
                                            port.setDefaultTax();
                                        }
                                        TravelNodesManager.createTravelNodeFromSign(sender, port, BlockFace.UP);
                                    } else {
                                        throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement above block").build());
                                    }
                                    break;
                                case NORTH, SOUTH, EAST, WEST:
                                    if(block.getRelative(blockFace).isEmpty()) {
                                        Location signLocation = block.getRelative(blockFace).getLocation();
                                        Port port = new Port(nodeName, portSize, signLocation, signLocation);
                                        if (AlathraPorts.getTownyHook().isTownyLoaded()) {
                                            port.findTown();
                                            port.setDefaultTax();
                                        }
                                        TravelNodesManager.createTravelNodeFromSign(sender, port, blockFace);
                                    } else {
                                        throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement on the block face").build());
                                    }
                                    break;
                                default:
                                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Abnormal block face found").build());
                            }
                        } else {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement").build());
                        }
                    }),
                new CommandAPICommand("carriage_station")
                    .withArguments(
                        new StringArgument("name")
                            .replaceSuggestions(ArgumentSuggestions.strings("Carriage_Name")),
                        CommandUtil.carriageStationSizeArgument("size")
                    ).executesPlayer((Player sender, CommandArguments args) -> {
                        CarriageStationSize carriageStationSize = (CarriageStationSize) args.get("size");
                        String nodeName = (String) args.get("name");
                        RayTraceResult rayTrace = sender.rayTraceBlocks(5, FluidCollisionMode.NEVER);
                        if (rayTrace != null) {
                            Block block = rayTrace.getHitBlock();
                            BlockFace blockFace = rayTrace.getHitBlockFace();
                            if (block == null) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, no base block").build());
                            }
                            if (blockFace == null) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, no base block face").build());
                            }
                            if (!(block.isSolid()) || !(block.isCollidable())) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, base block is not solid").build());
                            }
                            switch (blockFace) {
                                case UP, DOWN:
                                    if(block.getRelative(BlockFace.UP).isEmpty()) {
                                        Location signLocation = block.getRelative(BlockFace.UP).getLocation();
                                        CarriageStation carriageStation = new CarriageStation(nodeName, carriageStationSize, signLocation, signLocation);
                                        if (AlathraPorts.getTownyHook().isTownyLoaded()) {
                                            carriageStation.findTown();
                                            carriageStation.setDefaultTax();
                                        }
                                        TravelNodesManager.createTravelNodeFromSign(sender, carriageStation, BlockFace.UP);
                                    } else {
                                        throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement above block").build());
                                    }
                                    break;
                                case NORTH, SOUTH, EAST, WEST:
                                    if(block.getRelative(blockFace).isEmpty()) {
                                        Location signLocation = block.getRelative(blockFace).getLocation();
                                        CarriageStation carriageStation = new CarriageStation(nodeName, carriageStationSize, signLocation, signLocation);
                                        if (AlathraPorts.getTownyHook().isTownyLoaded()) {
                                            carriageStation.findTown();
                                            carriageStation.setDefaultTax();
                                        }
                                        TravelNodesManager.createTravelNodeFromSign(sender, carriageStation, blockFace);
                                    } else {
                                        throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement on the block face").build());
                                    }
                                    break;
                                default:
                                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Abnormal block face found").build());
                            }
                        } else {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement").build());
                        }
                    })
            );
    }

    public CommandAPICommand deleteCommand() {
        return new CommandAPICommand("delete")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
              new CommandAPICommand("port")
                  .withArguments(
                      CommandUtil.portArgument("targetPort")
                  )
                  .executesPlayer((Player sender, CommandArguments args) -> {
                      Port port = (Port) args.get("targetPort");
                      if (port == null) {
                          throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                      }
                      TravelNodesManager.deleteTravelNodeWithSign(sender, port);
                  }),
              new CommandAPICommand("carriage_station")
                  .withArguments(
                      CommandUtil.carriageStationArgument("targetCarriageStation")
                  )
                  .executesPlayer((Player sender, CommandArguments args) -> {
                      CarriageStation carriageStation = (CarriageStation) args.get("targetCarriageStation");
                      if (carriageStation == null) {
                          throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid carriage station argument").build());
                      }
                      TravelNodesManager.deleteTravelNodeWithSign(sender, carriageStation);
                  })
            );
    }

    public CommandAPICommand listCommand() {
        return new CommandAPICommand("list")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
                new CommandAPICommand("port")
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        String portList = "<yellow>Ports: ";
                        for (Port port : TravelNodesManager.getPorts()) {
                            portList += port.getName() + ", ";
                        }
                        sender.sendMessage(ColorParser.of(portList).build());
                    }),
                new CommandAPICommand("carriage_station")
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        String carriageStationList = "<yellow>Carriage Stations: ";
                        for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
                            carriageStationList += carriageStation.getName() + ", ";
                        }
                        sender.sendMessage(ColorParser.of(carriageStationList).build());
                    })
            );
    }

    public CommandAPICommand editCommand() {
        return new CommandAPICommand("edit")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
                new CommandAPICommand("port")
                    .withSubcommands(
                        new CommandAPICommand("name")
                            .withArguments(
                                CommandUtil.portArgument("targetPort"),
                                new StringArgument("newname")
                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                Port port = (Port) args.get("targetPort");
                                String newName = (String) args.get("newname");
                                if (port == null) {
                                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                                }
                                String oldName = port.getName();
                                port.setName(newName);
                                port.refreshNodeSign();
                                try {
                                    TravelNodesManager.reRegisterPort(port);
                                    if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                                        AlathraPorts.getDynmapHook().refreshPortMarker(port);
                                        AlathraPorts.getDynmapHook().refreshPortRangeMarker(port);
                                    }
                                    DBAction.saveAllPortsToDB();
                                    sender.sendMessage(ColorParser.of("<green>Port name has been changed").build());
                                } catch (TravelNodeRegisterException e) {
                                    // Revert sign change, registration failed
                                    port.setName(oldName);
                                    port.refreshNodeSign();
                                    Logger.get().warn(e.getMessage());
                                    sender.sendMessage(ColorParser.of("<red>Failed to update port registry. Check console for more details").build());
                                }
                            }),
                        new CommandAPICommand("size")
                            .withArguments(
                                CommandUtil.portArgument("targetPort"),
                                CommandUtil.portSizeArgument("newsize")
                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                Port port = (Port) args.get("targetPort");
                                PortSize newSize = (PortSize) args.get("newsize");
                                if (port == null) {
                                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                                }
                                PortSize oldSize = (PortSize) port.getSize();
                                port.setSize(newSize);
                                port.refreshNodeSign();
                                try {
                                    TravelNodesManager.reRegisterPort(port);
                                    port.setDefaultTax();
                                    if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                                        AlathraPorts.getDynmapHook().refreshPortMarker(port);
                                        AlathraPorts.getDynmapHook().refreshPortRangeMarker(port);
                                    }
                                    DBAction.saveAllPortsToDB();
                                    sender.sendMessage(ColorParser.of("<green>Port size has been changed").build());
                                } catch (TravelNodeRegisterException e) {
                                    // Revert sign change, registration failed
                                    port.setSize(oldSize);
                                    port.refreshNodeSign();
                                    Logger.get().warn(e.getMessage());
                                    sender.sendMessage(ColorParser.of("<red>Failed to update port registry. Check console for more details").build());
                                }
                            }),
                        new CommandAPICommand("teleport")
                            .withArguments(
                                CommandUtil.portArgument("targetPort")
                            )
                            .withOptionalArguments(
                                new LocationArgument("newteleport")
                            )
                            .executesPlayer((Player sender, CommandArguments args) -> {
                                Port port = (Port) args.get("targetPort");
                                Location newLocation = (Location) args.get("newteleport");
                                if (newLocation == null) {
                                    newLocation = sender.getLocation();
                                }
                                if (port == null) {
                                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                                }
                                Location oldLocation = port.getTeleportLocation();
                                port.setTeleportLocation(newLocation);
                                port.refreshNodeSign();
                                try {
                                    TravelNodesManager.reRegisterPort(port);
                                    if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                                        AlathraPorts.getDynmapHook().refreshPortMarker(port);
                                        AlathraPorts.getDynmapHook().refreshPortRangeMarker(port);
                                    }
                                    DBAction.saveAllPortsToDB();
                                    if (newLocation.equals(sender.getLocation())) {
                                        sender.sendMessage(ColorParser.of("<green>Port teleport location has been changed to your current location").build());
                                    } else {
                                        sender.sendMessage(ColorParser.of("<green>Port teleport location has been changed to the provided location").build());
                                    }
                                } catch (TravelNodeRegisterException e) {
                                    // Revert sign change, registration failed
                                    port.setTeleportLocation(oldLocation);
                                    port.refreshNodeSign();
                                    Logger.get().warn(e.getMessage());
                                    sender.sendMessage(ColorParser.of("<red>Failed to update port registry. Check console for more details").build());
                                }
                            })
                    ),
                new CommandAPICommand("carriage_station")
                    .withSubcommands(
                        new CommandAPICommand("name")
                            .withArguments(
                                CommandUtil.carriageStationArgument("targetCarriage_station"),
                                new StringArgument("newname")
                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                CarriageStation carriageStation = (CarriageStation) args.get("targetCarriage_station");
                                String newName = (String) args.get("newname");
                                if (carriageStation == null) {
                                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid carriage station argument").build());
                                }
                                String oldName = carriageStation.getName();
                                carriageStation.setName(newName);
                                carriageStation.refreshNodeSign();
                                try {
                                    TravelNodesManager.reRegisterCarriageStation(carriageStation);
                                    if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                                        AlathraPorts.getDynmapHook().refreshCarriageStationMarker(carriageStation);
                                        AlathraPorts.getDynmapHook().refreshCarriageStationConnectionMarkers(carriageStation);
                                    }
                                    DBAction.saveAllCarriageStationsToDB();
                                    sender.sendMessage(ColorParser.of("<green>Carriage station name has been changed").build());
                                } catch (TravelNodeRegisterException e) {
                                    // Revert sign change, registration failed
                                    carriageStation.setName(oldName);
                                    carriageStation.refreshNodeSign();
                                    Logger.get().warn(e.getMessage());
                                    sender.sendMessage(ColorParser.of("<red>Failed to update carriage station registry. Check console for more details").build());
                                }
                            }),
                        new CommandAPICommand("size")
                            .withArguments(
                                CommandUtil.carriageStationArgument("targetCarriage_station"),
                                CommandUtil.carriageStationSizeArgument("newsize")
                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                CarriageStation carriageStation = (CarriageStation) args.get("targetCarriage_station");
                                CarriageStationSize newSize = (CarriageStationSize) args.get("newsize");
                                if (carriageStation == null) {
                                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid carriage station argument").build());
                                }
                                CarriageStationSize oldSize = (CarriageStationSize) carriageStation.getSize();
                                carriageStation.setSize(newSize);
                                carriageStation.refreshNodeSign();
                                try {
                                    TravelNodesManager.reRegisterCarriageStation(carriageStation);
                                    carriageStation.setDefaultTax();
                                    if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                                        AlathraPorts.getDynmapHook().refreshCarriageStationMarker(carriageStation);
                                        AlathraPorts.getDynmapHook().refreshCarriageStationConnectionMarkers(carriageStation);
                                    }
                                    DBAction.saveAllCarriageStationsToDB();
                                    sender.sendMessage(ColorParser.of("<green>Carriage station size has been changed").build());
                                } catch (TravelNodeRegisterException e) {
                                    // Revert sign change, registration failed
                                    carriageStation.setSize(oldSize);
                                    carriageStation.refreshNodeSign();
                                    Logger.get().warn(e.getMessage());
                                    sender.sendMessage(ColorParser.of("<red>Failed to update carriage station registry. Check console for more details").build());
                                }
                            }),
                        new CommandAPICommand("teleport")
                            .withArguments(
                                CommandUtil.carriageStationArgument("targetCarriage_station")
                            )
                            .withOptionalArguments(
                                new LocationArgument("newteleport")
                            )
                            .executesPlayer((Player sender, CommandArguments args) -> {
                                CarriageStation carriageStation = (CarriageStation) args.get("targetCarriage_station");
                                Location newLocation = (Location) args.get("newteleport");
                                if (newLocation == null) {
                                    newLocation = sender.getLocation();
                                }
                                if (carriageStation == null) {
                                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid carriage station argument").build());
                                }
                                Location oldLocation = carriageStation.getTeleportLocation();
                                carriageStation.setTeleportLocation(newLocation);
                                carriageStation.refreshNodeSign();
                                try {
                                    TravelNodesManager.reRegisterCarriageStation(carriageStation);
                                    if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                                        AlathraPorts.getDynmapHook().refreshCarriageStationMarker(carriageStation);
                                        AlathraPorts.getDynmapHook().refreshCarriageStationConnectionMarkers(carriageStation);
                                    }
                                    DBAction.saveAllCarriageStationsToDB();
                                    if (newLocation.equals(sender.getLocation())) {
                                        sender.sendMessage(ColorParser.of("<green>Carriage station teleport location has been changed to your current location").build());
                                    } else {
                                        sender.sendMessage(ColorParser.of("<green>Carriage station teleport location has been changed to the provided location").build());
                                    }
                                } catch (TravelNodeRegisterException e) {
                                    // Revert sign change, registration failed
                                    carriageStation.setTeleportLocation(oldLocation);
                                    carriageStation.refreshNodeSign();
                                    Logger.get().warn(e.getMessage());
                                    sender.sendMessage(ColorParser.of("<red>Failed to update carriage station registry. Check console for more details").build());
                                }
                            })
                    )
            );
    }

    public CommandAPICommand moveCommand() {
        return new CommandAPICommand("move")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
                new CommandAPICommand("port")
                    .withArguments(
                        CommandUtil.portArgument("targetPort")
                    )
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        Port port = (Port) args.get("targetPort");
                        if (port == null) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                        }
                        RayTraceResult rayTrace = sender.rayTraceBlocks(5, FluidCollisionMode.NEVER);
                        if (rayTrace != null) {
                            Block block = rayTrace.getHitBlock();
                            BlockFace blockFace = rayTrace.getHitBlockFace();
                            if (block == null) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, no base block").build());
                            }
                            if (blockFace == null) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, no base block face").build());
                            }
                            if (!(block.isSolid()) || !(block.isCollidable())) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, base block is not solid").build());
                            }
                            Location oldSignLocation = port.getSignLocation();
                            try {
                                switch (blockFace) {
                                    case UP, DOWN:
                                        if (block.getRelative(BlockFace.UP).isEmpty()) {
                                            Location newSignLocation = block.getRelative(BlockFace.UP).getLocation();
                                            port.setSignLocation(newSignLocation);
                                            port.setTeleportLocation(newSignLocation);
                                            TravelNodesManager.reRegisterPort(port);
                                            DBAction.saveAllPortsToDB();
                                            if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                                                AlathraPorts.getDynmapHook().refreshPortMarker(port);
                                                AlathraPorts.getDynmapHook().refreshPortRangeMarker(port);
                                            }
                                        } else {
                                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement above block").build());
                                        }
                                        break;
                                    case NORTH, SOUTH, EAST, WEST:
                                        if (block.getRelative(blockFace).isEmpty()) {
                                            Location newSignLocation = block.getRelative(blockFace).getLocation();
                                            port.setSignLocation(newSignLocation);
                                            port.setTeleportLocation(newSignLocation);
                                            TravelNodesManager.reRegisterPort(port);
                                            DBAction.saveAllPortsToDB();
                                            if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                                                AlathraPorts.getDynmapHook().refreshPortMarker(port);
                                                AlathraPorts.getDynmapHook().refreshPortRangeMarker(port);
                                            }
                                        } else {
                                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement on the block face").build());
                                        }
                                        break;
                                    default:
                                        throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Abnormal block face found").build());
                                }
                                TravelNodesManager.placeTravelNodeSign(sender, port, blockFace);
                                oldSignLocation.getBlock().setType(Material.AIR);
                            } catch (TravelNodeRegisterException e) {
                                Logger.get().warn(e.getMessage());
                                sender.sendMessage(ColorParser.of("<red>Failed to update port registry. Check console for more details").build());
                            }
                        } else {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement").build());
                        }
                    }),

                new CommandAPICommand("carriage_station")
                    .withArguments(
                        CommandUtil.carriageStationArgument("targetCarriage_station")
                    )
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        CarriageStation carriageStation = (CarriageStation) args.get("targetCarriage_station");
                        if (carriageStation == null) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid carriage station argument").build());
                        }
                        RayTraceResult rayTrace = sender.rayTraceBlocks(5, FluidCollisionMode.NEVER);
                        if (rayTrace != null) {
                            Block block = rayTrace.getHitBlock();
                            BlockFace blockFace = rayTrace.getHitBlockFace();
                            if (block == null) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, no base block").build());
                            }
                            if (blockFace == null) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, no base block face").build());
                            }
                            if (!(block.isSolid()) || !(block.isCollidable())) {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement, base block is not solid").build());
                            }
                            Location oldSignLocation = carriageStation.getSignLocation();
                            try {
                                switch (blockFace) {
                                    case UP, DOWN:
                                        if (block.getRelative(BlockFace.UP).isEmpty()) {
                                            Location newSignLocation = block.getRelative(BlockFace.UP).getLocation();
                                            carriageStation.setSignLocation(newSignLocation);
                                            carriageStation.setTeleportLocation(newSignLocation);
                                            TravelNodesManager.reRegisterCarriageStation(carriageStation);
                                            DBAction.saveAllCarriageStationsToDB();
                                            if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                                                AlathraPorts.getDynmapHook().refreshCarriageStationMarker(carriageStation);
                                                AlathraPorts.getDynmapHook().refreshCarriageStationConnectionMarkers(carriageStation);
                                            }
                                        } else {
                                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement above block").build());
                                        }
                                        break;
                                    case NORTH, SOUTH, EAST, WEST:
                                        if (block.getRelative(blockFace).isEmpty()) {
                                            Location newSignLocation = block.getRelative(blockFace).getLocation();
                                            carriageStation.setSignLocation(newSignLocation);
                                            carriageStation.setTeleportLocation(newSignLocation);
                                            TravelNodesManager.reRegisterCarriageStation(carriageStation);
                                            DBAction.saveAllCarriageStationsToDB();
                                            if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                                                AlathraPorts.getDynmapHook().refreshCarriageStationMarker(carriageStation);
                                                AlathraPorts.getDynmapHook().refreshCarriageStationConnectionMarkers(carriageStation);
                                            }
                                        } else {
                                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement on the block face").build());
                                        }
                                        break;
                                    default:
                                        throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Abnormal block face found").build());
                                }
                                TravelNodesManager.placeTravelNodeSign(sender, carriageStation, blockFace);
                                oldSignLocation.getBlock().setType(Material.AIR);
                            } catch (TravelNodeRegisterException e) {
                                Logger.get().warn(e.getMessage());
                                sender.sendMessage(ColorParser.of("<red>Failed to update carriage station registry. Check console for more details").build());
                            }
                        } else {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement").build());
                        }
                    })
            );
    }

    public CommandAPICommand teleportCommand() {
        return new CommandAPICommand("teleport")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
                new CommandAPICommand("port")
                    .withArguments(
                        CommandUtil.portArgument("targetPort")
                    )
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        Port port = (Port) args.get("targetPort");
                        if (port == null) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                        }
                        sender.teleport(port.getTeleportLocation());
                    }),
                new CommandAPICommand("carriage_station")
                    .withArguments(
                        CommandUtil.carriageStationArgument("targetCarriage_station")
                    )
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        CarriageStation carriageStation = (CarriageStation) args.get("targetCarriage_station");
                        if (carriageStation == null) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid carriage station argument").build());
                        }
                        sender.teleport(carriageStation.getTeleportLocation());
                    })
            );
    }

    public CommandAPICommand reloadCommand() {
        return new CommandAPICommand("reload")
            .withPermission(ADMIN_PERM)
            .executesPlayer((Player sender, CommandArguments args) -> {
                AlathraPorts.getInstance().getConfigHandler().reloadConfig();
                sender.sendMessage(ColorParser.of("<yellow>Config settings reloaded").build());
            });
    }

    public CommandAPICommand blockade() {
        return new CommandAPICommand("blockade")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
                new CommandAPICommand("port")
                    .withArguments(
                        CommandUtil.portArgument("targetPort")
                    )
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        Port port = (Port) args.get("targetPort");
                        if (port == null) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                        }
                        if (port.isBlockaded()) {
                            port.setBlockaded(false);
                            sender.sendMessage(ColorParser.of("<yellow>The port of <light_purple>" + port.getName() + " <yellow>is no longer blockaded. This is not a global announcement").build());
                        } else {
                            port.setBlockaded(true);
                            sender.sendMessage(ColorParser.of("<yellow>The port of <light_purple>" + port.getName() + " <yellow>is now blockaded. This is not a global announcement").build());
                        }
                    }),
                new CommandAPICommand("carriage_station")
                    .withArguments(
                        CommandUtil.carriageStationArgument("targetCarriage_station")
                    )
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        CarriageStation carriageStation = (CarriageStation) args.get("targetCarriage_station");
                        if (carriageStation == null) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                        }
                        if (carriageStation.isBlockaded()) {
                            carriageStation.setBlockaded(false);
                            sender.sendMessage(ColorParser.of("<yellow>The carriage station of <light_purple>" + carriageStation.getName() + " <yellow>is no longer blockaded. This is not a global announcement").build());
                        } else {
                            carriageStation.setBlockaded(true);
                            sender.sendMessage(ColorParser.of("<yellow>The carriage station of <light_purple>" + carriageStation.getName() + " <yellow>is now blockaded. This is not a global announcement").build());
                        }
                    })
            );
    }

    public CommandAPICommand connect() {
        return new CommandAPICommand("connect")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
                new CommandAPICommand("carriage_station")
                    .withArguments(
                        CommandUtil.carriageStationArgument("firstCarriage_station"),
                        CommandUtil.carriageStationArgument("secondCarriage_station")
                    )
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        CarriageStation carriageStation1 = (CarriageStation) args.get("firstCarriage_station");
                        CarriageStation carriageStation2 = (CarriageStation) args.get("secondCarriage_station");
                        TravelNodesManager.connectCarriageStation(carriageStation1, carriageStation2, sender);
                    })
            );
    }

    public CommandAPICommand disconnect() {
        return new CommandAPICommand("disconnect")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
                new CommandAPICommand("carriage_station")
                    .withArguments(
                        CommandUtil.carriageStationArgument("firstCarriage_station"),
                        CommandUtil.carriageStationArgument("secondCarriage_station")
                    )
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        CarriageStation carriageStation1 = (CarriageStation) args.get("firstCarriage_station");
                        CarriageStation carriageStation2 = (CarriageStation) args.get("secondCarriage_station");
                        TravelNodesManager.disconnectCarriageStations(carriageStation1, carriageStation2, sender);
                    })
            );
    }

    public CommandAPICommand listConnections() {
        return new CommandAPICommand("list_connections")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
                new CommandAPICommand("carriage_station")
                    .withArguments(
                        CommandUtil.carriageStationArgument("targetCarriage_station")
                    )
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        CarriageStation carriageStation = (CarriageStation) args.get("targetCarriage_station");
                        if (carriageStation == null) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid carriage station argument(s)").build());
                        }
                        String directConnections = "<yellow>Direct Connections: ";
                        for (TravelNode connection : carriageStation.getDirectConnections()) {
                            directConnections += connection.getName() + ", ";
                        }
                        sender.sendMessage(ColorParser.of(directConnections).build());
                    })
            );
    }


}
