package com.example.authtest.Model;

public class RestaurantReview {
    public int id;
    public int review;
    public double latitude;
    public double longitude;

    public double getrLat() { return latitude; }

    public double getrLon() { return longitude; }

    public int getrId() { return id; }

    public int getrReview(){ return review; }

    public void setrLat(double latitude) { this.latitude = latitude; }

    public void setrLon(double longitude) { this.longitude = longitude; }

    public void setrId(int id) { this.id = id; }

    public void setrReview(int review) { this.review = review; }

}
