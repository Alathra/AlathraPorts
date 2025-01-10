package io.github.alathra.alathraports.command;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.travelnodes.ports.Port;
import io.github.alathra.alathraports.travelnodes.ports.PortSize;
import io.github.alathra.alathraports.travelnodes.TravelNodesManager;
import io.github.alathra.alathraports.travelnodes.exceptions.TravelNodeRegisterException;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

class PortsCommand {
    private static final String ADMIN_PERM = "alathraports.admin";

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
                blockade()
            )
            .executesPlayer(this::helpCommand)
            .register();
    }

    private void helpCommand(CommandSender sender, CommandArguments args) {
    }

    public CommandAPICommand createCommand() {
        return new CommandAPICommand("create")
            .withPermission(ADMIN_PERM)
            .withArguments(
                new StringArgument("name")
                    .replaceSuggestions(ArgumentSuggestions.strings("Port_Name")),
                CommandUtil.portSizeArgument("size")
            )
            .executesPlayer((Player sender, CommandArguments args) -> {
                PortSize portSize = (PortSize) args.get("size");
                String portName = (String) args.get("name");
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
                                Port port = new Port(portName, portSize, signLocation, signLocation);
                                if (AlathraPorts.getTownyHook().isTownyLoaded()) {
                                    port.findTown();
                                }
                                TravelNodesManager.createTravelNodeFromSign(sender, port, BlockFace.UP);
                            } else {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement above block").build());
                            }
                            break;
                        case NORTH, SOUTH, EAST, WEST:
                            if(block.getRelative(blockFace).isEmpty()) {
                                Location signLocation = block.getRelative(blockFace).getLocation();
                                Port port = new Port(portName, portSize, signLocation, signLocation);
                                if (AlathraPorts.getTownyHook().isTownyLoaded()) {
                                    port.findTown();
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
            });
    }

    public CommandAPICommand deleteCommand() {
        return new CommandAPICommand("delete")
            .withPermission(ADMIN_PERM)
            .withArguments(
                CommandUtil.portArgument("port")
            )
            .executesPlayer((Player sender, CommandArguments args) -> {
                Port port = (Port) args.get("port");
                TravelNodesManager.deleteTravelNodeWithSign(sender, port);
            });
    }

    public CommandAPICommand listCommand() {
        return new CommandAPICommand("list")
            .withPermission(ADMIN_PERM)
            .executesPlayer((Player sender, CommandArguments args) -> {
                String portList = "<yellow>Ports: ";
                for (Port port : TravelNodesManager.getPorts()) {
                    portList += port.getName() + ", ";
                }
                sender.sendMessage(ColorParser.of(portList).build());
            });
    }

    public CommandAPICommand editCommand() {
        return new CommandAPICommand("edit")
            .withPermission(ADMIN_PERM)
            .withSubcommands(
                new CommandAPICommand("name")
                    .withArguments(
                        CommandUtil.portArgument("port"),
                        new StringArgument("newname")
                    ).executesPlayer((Player sender, CommandArguments args) -> {
                        Port port = (Port) args.get("port");
                        String newName = (String) args.get("newname");
                        if (port == null) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                        }
                        String oldName = port.getName();
                        port.setName(newName);
                        if (!port.refreshNodeSign()) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Port sign has been moved or is missing").build());
                        }
                        try {
                            TravelNodesManager.reRegisterPort(port);
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
                        CommandUtil.portArgument("port"),
                        CommandUtil.portSizeArgument("newsize")
                    ).executesPlayer((Player sender, CommandArguments args) -> {
                        Port port = (Port) args.get("port");
                        PortSize newSize = (PortSize) args.get("newsize");
                        if (port == null) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                        }
                        PortSize oldSize = (PortSize) port.getSize();
                        port.setSize(newSize);
                        if (!port.refreshNodeSign()) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Port sign has been moved or is missing").build());
                        }
                        try {
                            TravelNodesManager.reRegisterPort(port);
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
                        CommandUtil.portArgument("port")
                    )
                    .withOptionalArguments(
                        new LocationArgument("newteleport")
                    )
                    .executesPlayer((Player sender, CommandArguments args) -> {
                        Port port = (Port) args.get("port");
                        Location newLocation = (Location) args.get("newteleport");
                        if (newLocation == null) {
                            newLocation = sender.getLocation();
                        }
                        if (port == null) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                        }
                        Location oldLocation = port.getTeleportLocation();
                        port.setTeleportLocation(newLocation);
                        if (!port.refreshNodeSign()) {
                            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Port sign has been moved or is missing").build());
                        }
                        try {
                            TravelNodesManager.reRegisterPort(port);
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
            );
    }

    public CommandAPICommand moveCommand() {
        return new CommandAPICommand("move")
            .withPermission(ADMIN_PERM)
            .withArguments(
                CommandUtil.portArgument("port")
            )
            .executesPlayer((Player sender, CommandArguments args) -> {
                Port port = (Port) args.get("port");
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
                        TravelNodesManager.reRegisterPort(port);
                        switch (blockFace) {
                            case UP, DOWN:
                                if (block.getRelative(BlockFace.UP).isEmpty()) {
                                    Location newSignLocation = block.getRelative(BlockFace.UP).getLocation();
                                    port.setSignLocation(newSignLocation);
                                    port.setTeleportLocation(newSignLocation);
                                } else {
                                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement above block").build());
                                }
                                break;
                            case NORTH, SOUTH, EAST, WEST:
                                if (block.getRelative(blockFace).isEmpty()) {
                                    Location newSignLocation = block.getRelative(blockFace).getLocation();
                                    port.setSignLocation(newSignLocation);
                                    port.setTeleportLocation(newSignLocation);
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
            });
    }

    public CommandAPICommand teleportCommand() {
        return new CommandAPICommand("teleport")
            .withPermission(ADMIN_PERM)
            .withArguments(
                CommandUtil.portArgument("port")
            )
            .executesPlayer((Player sender, CommandArguments args) -> {
                Port port = (Port) args.get("port");
                if (port == null) {
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid port argument").build());
                }
                sender.teleport(port.getTeleportLocation());
            });
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
            .withArguments(
                CommandUtil.portArgument("port")
            )
            .executesPlayer((Player sender, CommandArguments args) -> {
                Port port = (Port) args.get("port");
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
            });
    }


}
