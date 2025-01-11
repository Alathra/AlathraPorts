package io.github.alathra.alathraports.command;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.travelnodes.TravelNode;
import io.github.alathra.alathraports.travelnodes.carriagestations.CarriageStation;
import io.github.alathra.alathraports.travelnodes.carriagestations.CarriageStationSize;
import io.github.alathra.alathraports.travelnodes.ports.Port;
import io.github.alathra.alathraports.travelnodes.ports.PortSize;
import io.github.alathra.alathraports.travelnodes.TravelNodesManager;

import java.util.Arrays;

public abstract class CommandUtil {

    public static Argument<TravelNode.TravelNodeType> travelNodeTypeArgument (String nodeType) {

        return new CustomArgument<TravelNode.TravelNodeType, String>(new StringArgument(nodeType), info -> {
            TravelNode.TravelNodeType type;
            try {
                type = TravelNode.TravelNodeType.valueOf(nodeType);
            } catch (IllegalArgumentException e) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of("<Red>Invalid travel node type argument").build());
            }
            return type;
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
            Arrays.stream(TravelNode.TravelNodeType.values()).map(Enum::name).toArray(String[]::new)
        ));
    }

    public static Argument<PortSize> portSizeArgument(String sizeName) {
        return new CustomArgument<PortSize, String>(new StringArgument(sizeName), info -> {
            PortSize size = TravelNodesManager.getPortSizeByName(info.input());
            if (size == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of("<Red>Invalid port size argument").build());
            }
            return size;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info ->
            Settings.portSizes.keySet().stream().toList()
        ));
    }

    public static Argument<CarriageStationSize> carriageStationSizeArgument(String sizeName) {
        return new CustomArgument<CarriageStationSize, String>(new StringArgument(sizeName), info -> {
            CarriageStationSize size = TravelNodesManager.getCarriageStationSizeByName(info.input());
            if (size == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of("<Red>Invalid carriage station size argument").build());
            }
            return size;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info ->
            Settings.carriageStationSizes.keySet().stream().toList()
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

    public static Argument<CarriageStation> carriageStationArgument(String nodeName) {
        return new CustomArgument<CarriageStation, String>(new StringArgument(nodeName), info -> {
            CarriageStation carriageStation = TravelNodesManager.getCarriageStationByName(info.input());
            if (carriageStation == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of("<Red>Invalid carriage station name argument").build());
            }
            return carriageStation;
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
            TravelNodesManager.getCarriageStations().stream().map(CarriageStation::getName).toArray(String[]::new)
        ));
    }
}
