package com.example.gasolinerafirebase;

public class Note {
    private String title;
    private String description;
    private int priority;
    private double lat;

    private double lng;

    public Note() {
        //empty constructor needed
    }

    public Note(String title, String description, int priority, double lat, double lng) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.lat = lat;
        this.lng = lng;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public double getLatitud() {
        return lat;
    }

    public double getLongitud() {
        return lng;
    }
}
