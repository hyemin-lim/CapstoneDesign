package com.example.maptest.Model;

public class StreetLight {
    public String street_light_id;
    public Double street_light_x;
    public Double street_light_y;

    public String getId(){
        return street_light_id;
    }

    public double getX() {
        return street_light_x;
    }

    public double getY() {
        return street_light_y;
    }

    public void setId(String id) {
        this.street_light_id = id;
    }

    public void setX(double x) {
        this.street_light_x = x;
    }

    public void setY(double y) {
        this.street_light_y = y;
    }
}
