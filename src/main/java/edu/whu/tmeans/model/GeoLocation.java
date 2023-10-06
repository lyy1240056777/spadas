package edu.whu.tmeans.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.lucene.spatial3d.geom.GeoDistance;

@Data
@AllArgsConstructor
public class GeoLocation {

    public static final int EARTH_RADIUS = 6371;

    private double lat;

    private double lon;


    public Double distance(GeoLocation o) {
        return calculateDistance(lat, lon, o.lat, o.lon);
    }

    private double calculateDistance(double startLat, double startLong, double endLat, double endLong) {

        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
