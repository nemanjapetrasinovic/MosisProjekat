package com.example.nemanja.mosisprojekat;

import java.util.ArrayList;
import java.util.List;

class Traveller {
    public String firstname;
    public String lastname;
    public String phonenumber;
    public int score;
    public List<String> friends;
    public String picture;
    public double latitude;
    public double longitude;
    public String email;
    public List<String> places;
    public List<String> visitedPlaces;
    public List<String> myPlaces;
    public String image;
    public String address;
    public double homelat;
    public double homelon;

    public List<String> getPlaces() {
        return places;
    }

    public void setPlaces(List<String> places) {
        this.places = places;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public int getScore() {
        return score;
    }

    public List<String> getFriends() {
        return friends;
    }

    public String getPicture() {
        return picture;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public void addFriend(String friend){
        this.friends.add(friend);
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getEmail() {
        return email;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getVisitedPlaces() {
        return visitedPlaces;
    }

    public void setVisitedPlaces(List<String> visitedPlaces) {
        this.visitedPlaces = visitedPlaces;
    }

    public List<String> getMyPlaces() {
        return myPlaces;
    }

    public void setMyPlaces(List<String> myPlaces) {
        this.myPlaces = myPlaces;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
