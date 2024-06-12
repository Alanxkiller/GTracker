package com.example.gasolinerafirebase;

import com.google.android.gms.maps.model.LatLng;

public class LocationManager {
    private static LocationManager instance;
    private LatLng currentLocation;

    private LocationManager() {
        // Constructor privado para evitar instanciación directa
    }

    public static synchronized LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }

    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LatLng location) {
        this.currentLocation = location;
    }

    public double getCurrentLatitude() {
        return currentLocation != null ? currentLocation.latitude : 0.0;
    }

    public double getCurrentLongitude() {
        return currentLocation != null ? currentLocation.longitude : 0.0;
    }
}
