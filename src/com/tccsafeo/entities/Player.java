package com.tccsafeo.entities;

public class Player
{
    private String id;

    private String name;

    private String federation;

    private Integer rating;

    public Player(){}

    public Player(String id, String name, String federation, Integer rating) {
        this.id = id;
        this.name = name;
        this.federation = federation;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFederation() {
        return federation;
    }

    public void setFederation(String federation) {
        this.federation = federation;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
