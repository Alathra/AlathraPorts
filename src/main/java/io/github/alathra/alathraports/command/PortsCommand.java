package io.github.alathra.alathraports.command;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.alathra.alathraports.ports.Port;
import io.github.alathra.alathraports.ports.Ports;
import io.github.alathra.alathraports.ports.enums.PortSize;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

/**
 * Class containing the code for the example command.
 */
class PortsCommand {
    private static final String ADMIN_PERM = "alathraports.admin";

    /**
     * Instantiates and registers a new command.
     */
    protected PortsCommand() {
        new CommandAPICommand("ports")
            .withFullDescription("Example command.")
            .withShortDescription("Example command.")
            .withSubcommands(
                createCommand(),
                deleteCommand(),
                listCommand()
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
                new StringArgument("portname")
                    .replaceSuggestions(ArgumentSuggestions.strings("Port_Name")),
                CommandUtil.portSizeArgument("portsize")
            )
            .executesPlayer((Player sender, CommandArguments args) -> {
                PortSize portSize = (PortSize) args.get("portsize");
                String portName = (String) args.get("portname");
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
                                Ports.createPortFromSign(sender, new Port(portName, portSize, signLocation, signLocation), BlockFace.UP);
                            } else {
                                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Could not find safe sign placement above block").build());
                            }
                            break;
                        case NORTH, SOUTH, EAST, WEST:
                            if(block.getRelative(blockFace).isEmpty()) {
                                Location signLocation = block.getRelative(blockFace).getLocation();
                                Ports.createPortFromSign(sender, new Port(portName, portSize, signLocation, signLocation), blockFace);
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
                CommandUtil.portArgument("portname")
            )
            .executesPlayer((Player sender, CommandArguments args) -> {
                Port port = (Port) args.get("portname");
                Ports.deletePortFromSign(sender, port);
            });
    }

    public CommandAPICommand listCommand() {
        return new CommandAPICommand("list")
            .withPermission(ADMIN_PERM)
            .executesPlayer((Player sender, CommandArguments args) -> {
                String portList = "<yellow>Ports: ";
                for (Port port : Ports.getPorts()) {
                    portList += port.getName() + ", ";
                }
                sender.sendMessage(ColorParser.of(portList).build());
            });
    }
}
