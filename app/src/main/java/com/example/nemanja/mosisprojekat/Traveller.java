package com.example.nemanja.mosisprojekat;

import java.util.List;

class Traveller {
    public String firstname;
    public String lastname;
    public String phonenumber;
    public int score;
    public List<String> friends;
    public String picture;
    public float latitude;
    public float longitude;
    public String email;

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
    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
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
}
