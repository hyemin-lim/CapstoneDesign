package com.example.authtest.Model;

public class Restaurant{//TODO: restaurant object class
    public String id;
    public double x;
    public double y;
    public String level;
    public String name;

    public double getY() { return y; }

    public double getX() { return x; }

    public String getId() { return id; }

    public String getLevel() { return level; }

    public String getName() { return name; }

    public void setY(double y) { this.y = y; }

    public void setX(double x) { this.x = x; }

    public void setId(String id) { this.id = id; }

    public void setLevel(String level) { this.level = level; }

    public void setName(String name) { this.name = name; }
}

