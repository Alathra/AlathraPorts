package io.github.alathra.alathraports.hook.dynmap;

import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.core.TravelNode;
import io.github.alathra.alathraports.core.TravelNodesManager;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.core.ports.PortSize;
import io.github.alathra.alathraports.hook.AbstractHook;
import io.github.alathra.alathraports.hook.Hook;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.*;

public class DynmapHook extends AbstractHook {

    private DynmapCommonAPI dynmapAPI;
    private MarkerAPI markerAPI;

    private MarkerSet mainMarkerSet;
    private MarkerSet portRangeMarkerSet;
    private MarkerSet carriageConnectionsMarkerSet;
    private MarkerIcon portIcon;
    private MarkerIcon carriageStationIcon;

    public DynmapHook(AlathraPorts plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AlathraPorts plugin) {
        if (!isHookLoaded())
            return;
    }

    @Override
    public void onEnable(AlathraPorts plugin) {
        if (!isHookLoaded())
            return;

        dynmapAPI = (DynmapCommonAPI) plugin.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapAPI != null) {
            markerAPI = dynmapAPI.getMarkerAPI();
        }

        initializeTravelNodeMarkers();
        initializePortRangeMarkers();
        initializeCarriageConnectionMarkers();
    }

    @Override
    public void onDisable(AlathraPorts plugin) {
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.Dynmap.getPluginName()) && isPluginEnabled(Hook.Dynmap.getPluginName());
    }

    // should be called on server enable
    private void initializeTravelNodeMarkers() {
        // initialize marker set
        createMainMarkerSet();
        // clear all existing markers
        clearAllMarkers(mainMarkerSet);
        placeAllPortMarkers();
        placeAllCarriageStationMarkers();
    }

    // should be called on server enable
    private void initializePortRangeMarkers() {
        // initialize marker set
        createPortRangeMarkerSet();
        // clear all existing markers
        clearAllMarkers(portRangeMarkerSet);
        placeAllPortRangeMarkers();
    }

    // should be called on server enable
    private void initializeCarriageConnectionMarkers() {
        // initialize marker set
        createCarriageConnectionsMarkerSet();
        // clear all existing markers
        clearAllMarkers(carriageConnectionsMarkerSet);
        placeAllCarriageConnectionMarkers();
    }

    private void createMainMarkerSet() {
        mainMarkerSet = markerAPI.getMarkerSet("ports.markerset");
        if (mainMarkerSet == null) {
            mainMarkerSet = markerAPI.createMarkerSet("ports.markerset", "Ports and Carriages", null, false);
        } else {
            mainMarkerSet.setMarkerSetLabel("Ports and Carriages");
        }
        mainMarkerSet.setMinZoom(0);
        mainMarkerSet.setLayerPriority(Settings.TRAVEL_NODES_DYNMAP_LAYER);
        mainMarkerSet.setHideByDefault(false);
        portIcon = markerAPI.getMarkerIcon("anchor");
        carriageStationIcon = markerAPI.getMarkerIcon("sign");
        mainMarkerSet.addAllowedMarkerIcon(portIcon);
        mainMarkerSet.addAllowedMarkerIcon(carriageStationIcon);
    }

    private void createPortRangeMarkerSet() {
        portRangeMarkerSet = markerAPI.getMarkerSet("portranges.markerset");
        if (portRangeMarkerSet == null) {
            portRangeMarkerSet = markerAPI.createMarkerSet("portranges.markerset", "Port Ranges", null, false);
        } else {
            portRangeMarkerSet.setMarkerSetLabel("Port Ranges");
        }
        portRangeMarkerSet.setMinZoom(0);
        portRangeMarkerSet.setLayerPriority(Settings.PORT_RANGES_DYNMAP_LAYER);
        portRangeMarkerSet.setHideByDefault(true);
    }

    private void createCarriageConnectionsMarkerSet() {
        carriageConnectionsMarkerSet = markerAPI.getMarkerSet("carriageconnections.markerset");
        if (carriageConnectionsMarkerSet == null) {
            carriageConnectionsMarkerSet = markerAPI.createMarkerSet("carriageconnections.markerset", "Carriage Connections", null, false);
        } else {
            carriageConnectionsMarkerSet.setMarkerSetLabel("Carriage Connections");
        }
        carriageConnectionsMarkerSet.setMinZoom(0);
        carriageConnectionsMarkerSet.setLayerPriority(Settings.CARRIAGE_CONNECTIONS_DYNMAP_LAYER);
        carriageConnectionsMarkerSet.setHideByDefault(true);
    }

    public void clearAllMarkers(MarkerSet markerSet) {
        if (markerSet == null) {
            return;
        }
        for (Marker marker : markerSet.getMarkers()) {
            if (marker != null) {
                marker.deleteMarker();
            }
        }
        for (CircleMarker circleMarker : markerSet.getCircleMarkers()) {
            if (circleMarker != null) {
                circleMarker.deleteMarker();
            }
        }
        for (PolyLineMarker polyLineMarker : markerSet.getPolyLineMarkers()) {
            if (polyLineMarker != null) {
                polyLineMarker.deleteMarker();
            }
        }
    }

    private void placeAllPortMarkers() {
        for (Port port : TravelNodesManager.getPorts()) {
            placePortMarker(port);
        }
    }

    private void placeAllCarriageStationMarkers() {
        for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
            placeCarriageStationMarker(carriageStation);
        }
    }

    private void placeAllPortRangeMarkers() {
        for (Port port : TravelNodesManager.getPorts()) {
            placePortRangeMarker(port);
        }
    }

    public void placeAllCarriageConnectionMarkers() {
        for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
            for (TravelNode connectedNode : carriageStation.getDirectConnections()) {
                CarriageStation directConnection = (CarriageStation) connectedNode;
                placeCarriageConnectionMarker(carriageStation, directConnection);
            }
        }
    }

    public void placePortMarker(Port port) {
        String markerID = port.getUuid() + "__port";
        String infobox =
            "<div class=\\\"regioninfo\\\">" +
                "<div class=\"\\infowindow\\\">" +
                "<span style=\"font-size: 120%;\">Port of @portname</span><br/>" +
                "Type: <span style=\"font-weight: bold;\">@portsize</span><br/>" +
                "Tax Rate: <span style=\"font-weight: bold;\">@porttax</span>" +
                "</div>" +
                "</div>";
        infobox = infobox.replaceAll("@portname", port.getName());
        infobox = infobox.replaceAll("@portsize", port.getSize().getName());
        infobox = infobox.replaceAll("@porttax", (int) (port.getTownFee() * 100) + "%");
        Marker marker = mainMarkerSet.createMarker
            (
                markerID,
                "Port of " + port.getName(),
                port.getWorld().getName(),
                port.getSignLocation().getX(),
                port.getSignLocation().getY(),
                port.getSignLocation().getZ(),
                portIcon,
                false
            );
        if (marker == null) {
            return;
        }
        marker.setDescription(infobox);
    }

    public void placeCarriageStationMarker(CarriageStation carriageStation) {
        String markerID = carriageStation.getUuid() + "__carriage_station";
        String infobox =
            "<div class=\\\"regioninfo\\\">" +
                "<div class=\"\\infowindow\\\">" +
                "<span style=\"font-size: 120%;\">Carriage Station of @carriagestationname</span><br/>" +
                "Type: <span style=\"font-weight: bold;\">@carriagestationsize</span><br/>" +
                "Tax Rate: <span style=\"font-weight: bold;\">@carriagestationtax</span>" +
                "</div>" +
                "</div>";
        infobox = infobox.replaceAll("@carriagestationname", carriageStation.getName());
        infobox = infobox.replaceAll("@carriagestationsize", carriageStation.getSize().getName());
        infobox = infobox.replaceAll("@carriagestationtax", (int) (carriageStation.getTownFee() * 100) + "%");
        Marker marker = mainMarkerSet.createMarker
            (
                markerID,
                "Carriage Station of " + carriageStation.getName(),
                carriageStation.getWorld().getName(),
                carriageStation.getSignLocation().getX(),
                carriageStation.getSignLocation().getY(),
                carriageStation.getSignLocation().getZ(),
                carriageStationIcon,
                false
            );
        if (marker == null) {
            return;
        }
        marker.setDescription(infobox);
    }

    public void placePortRangeMarker(Port port) {
        String markerID = port.getUuid() + "__port_range";
        int range = ((PortSize) port.getSize()).getRange();
        CircleMarker circleMarker = portRangeMarkerSet.createCircleMarker(
            markerID, // String ID
            "Range of Port " + port.getName(), // Label
            false, // is Label using markup
            port.getWorld().getName(), // World name
            port.getSignLocation().getX(), // X Center Location
            port.getSignLocation().getY(), // Y Center Location
            port.getSignLocation().getZ(), // Z Center Location
            range, // X radius
            range, // Z radius
            true // Is persistent
        );
        if (circleMarker == null) {
            return;
        }
        circleMarker.setFillStyle(
            0.05, // Opacity %
            0xFF0000 // Color (Red)
        );
    }

    public void placeCarriageConnectionMarker(CarriageStation carriageStation1, CarriageStation carriageStation2) {
        String markerID = carriageStation1.getUuid() + "-" + carriageStation2.getUuid() + "__carriage_connection";
        PolyLineMarker polyLineMarker =  carriageConnectionsMarkerSet.createPolyLineMarker(
            markerID, // String ID
            carriageStation1.getName() + "-" + carriageStation2.getName() + " Connection", // Label
            false, // is Label using markup
            carriageStation1.getWorld().getName(), // World name
            new double[] {carriageStation1.getSignLocation().getX(), carriageStation2.getSignLocation().getX()}, // X Coords of Line
            new double[] {carriageStation1.getSignLocation().getY(), carriageStation2.getSignLocation().getY()}, // Y Coords of Line
            new double[] {carriageStation1.getSignLocation().getZ(), carriageStation2.getSignLocation().getZ()}, // Z Coords of Line
            false // Is persistent
        );
    }

    public void removePortMarker(Port port) {
        Marker marker = mainMarkerSet.findMarker(port.getUuid()+ "__port");
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    public void removeCarriageStationMarker(CarriageStation carriageStation) {
        Marker marker = mainMarkerSet.findMarker(carriageStation.getUuid() + "__carriage_station");
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    public void removePortRangeMarker(Port port) {
        CircleMarker circleMarker = portRangeMarkerSet.findCircleMarker(port.getUuid() + "__port_range");
        if (circleMarker != null) {
            circleMarker.deleteMarker();
        }
    }

    public void removeCarriageStationConnectionMarker(CarriageStation carriageStation1, CarriageStation carriageStation2) {
        String markerID1 = carriageStation1.getUuid() + "-" + carriageStation2.getUuid() + "__carriage_connection";
        String markerID2 = carriageStation2.getUuid() + "-" + carriageStation1.getUuid() + "__carriage_connection";
        PolyLineMarker polyLineMarker1 = carriageConnectionsMarkerSet.findPolyLineMarker(markerID1);
        PolyLineMarker polyLineMarker2 = carriageConnectionsMarkerSet.findPolyLineMarker(markerID2);
        if (polyLineMarker1 != null) {
            polyLineMarker1.deleteMarker();
        }
        if (polyLineMarker2 != null) {
            polyLineMarker2.deleteMarker();
        }
    }

    public void removeCarriageStationConnectionMarkers(CarriageStation carriageStation) {
        for (TravelNode connection : carriageStation.getDirectConnections()) {
            removeCarriageStationConnectionMarker(carriageStation, (CarriageStation) connection);
        }
    }

    public void refreshPortMarker(Port port) {
        removePortMarker(port);
        removePortRangeMarker(port);
    }

    public void refreshPortRangeMarker(Port port) {
        placePortMarker(port);
        placePortRangeMarker(port);
    }

    public void refreshCarriageStationMarker(CarriageStation carriageStation) {
        removeCarriageStationMarker(carriageStation);
        placeCarriageStationMarker(carriageStation);
    }

    public void refreshCarriageStationConnectionMarker(CarriageStation carriageStation1, CarriageStation carriageStation2) {
        removeCarriageStationConnectionMarker(carriageStation1, carriageStation2);
        placeCarriageConnectionMarker(carriageStation1, carriageStation2);
    }

    public void refreshCarriageStationConnectionMarkers(CarriageStation carriageStation) {
        for (TravelNode connection : carriageStation.getDirectConnections()) {
            removeCarriageStationConnectionMarker(carriageStation, (CarriageStation) connection);
            placeCarriageConnectionMarker(carriageStation, (CarriageStation) connection);
        }
    }

    public void refreshAllMarkers() {
        clearAllMarkers(mainMarkerSet);
        clearAllMarkers(portRangeMarkerSet);
        clearAllMarkers(carriageConnectionsMarkerSet);
        placeAllPortMarkers();
        placeAllCarriageStationMarkers();
        placeAllPortRangeMarkers();
        placeAllCarriageConnectionMarkers();
    }
}