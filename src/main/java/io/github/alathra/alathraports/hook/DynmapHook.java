package io.github.alathra.alathraports.hook;

import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.Reloadable;
import io.github.alathra.alathraports.core.TravelNodesManager;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.ports.Port;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

public class DynmapHook implements Reloadable {
    private final AlathraPorts plugin;
    private DynmapAPI dynmapAPI;
    private MarkerAPI markerAPI;

    private MarkerSet markerSet;
    private MarkerIcon portIcon;
    private MarkerIcon carriageStationIcon;

    public DynmapHook(AlathraPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        if(!isDynmapLoaded()) {
            return;
        }

        dynmapAPI = (DynmapAPI) plugin.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapAPI != null) {
            markerAPI = dynmapAPI.getMarkerAPI();
        }

    }

    @Override
    public void onDisable() {

    }

    public boolean isDynmapLoaded() {
        return plugin.getServer().getPluginManager().isPluginEnabled("dynmap");
    }

    public void initMarkerSet() {
        markerSet = markerAPI.getMarkerSet("ports.markerset");
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("ports.markerset", "Ports", null, false);
        } else {
            markerSet.setMarkerSetLabel("Ports and Carriages");
        }
        markerSet.setMinZoom(0);
        markerSet.setLayerPriority(3);
        markerSet.setHideByDefault(false);
        portIcon = markerAPI.getMarkerIcon("anchor");
        carriageStationIcon = markerAPI.getMarkerIcon("sign");
        markerSet.addAllowedMarkerIcon(portIcon);
        markerSet.addAllowedMarkerIcon(carriageStationIcon);
    }

    public void placeAllPortMarkers() {
        for (Port port : TravelNodesManager.getPorts()) {
            String markerID = port.getName() + "__port";
            Marker marker = markerSet.createMarker
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
            marker.setDescription(infobox);
        }
    }

    public void placeAllCarriageStationMarkers() {
        for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
            String markerID = carriageStation.getName() + "__carriage_station";
            Marker marker = markerSet.createMarker
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
            String infobox =
                "<div class=\\\"regioninfo\\\">" +
                    "<div class=\"\\infowindow\\\">" +
                        "<span style=\"font-size: 120%;\">Port of @carriagestationname</span><br/>" +
                        "Type: <span style=\"font-weight: bold;\">@carriagestationsize</span><br/>" +
                        "Tax Rate: <span style=\"font-weight: bold;\">@carriagestationtax</span>" +
                    "</div>" +
                "</div>";
            infobox = infobox.replaceAll("@carriagestationname", carriageStation.getName());
            infobox = infobox.replaceAll("@carriagestationsize", carriageStation.getSize().getName());
            infobox = infobox.replaceAll("@carriagestationtax", (int) (carriageStation.getTownFee() * 100) + "%");
            marker.setDescription(infobox);
        }
    }

    public void clearAllMarkers() {
        for (Marker marker : markerSet.getMarkers()) {
            marker.deleteMarker();
        }
    }

    public void resetMarkers() {
        if (markerSet != null) {
            markerSet.deleteMarkerSet();
            markerSet = null;
        }
        initMarkerSet();
        clearAllMarkers();
        placeAllPortMarkers();
        placeAllCarriageStationMarkers();
        placeAllCarriageStationMarkers();
    }


}
