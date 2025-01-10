package io.github.alathra.alathraports.command;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.travelnodes.ports.Port;
import io.github.alathra.alathraports.travelnodes.ports.PortSize;
import io.github.alathra.alathraports.travelnodes.TravelNodesManager;

public abstract class CommandUtil {

    public static Argument<PortSize> portSizeArgument(String nodeName) {
        return new CustomArgument<PortSize, String>(new StringArgument(nodeName), info -> {
            PortSize size = TravelNodesManager.getPortSizeByName(info.input());
            if (size == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of("<Red>Invalid port size argument").build());
            }
            return size;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info ->
            Settings.portSizes.keySet().stream().toList()
        ));
    }

    public static Argument<Port> portArgument(String nodeName) {
        return new CustomArgument<Port, String>(new StringArgument(nodeName), info -> {
            Port port = TravelNodesManager.getPortByName(info.input());
            if (port == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of("<Red>Invalid port name argument").build());
            }
            return port;
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
            TravelNodesManager.getPorts().stream().map(Port::getName).toArray(String[]::new)
        ));
    }
}
