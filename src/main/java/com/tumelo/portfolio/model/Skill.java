package com.tumelo.portfolio.model;

public class Skill {

    private String name;
    private String imageId;

    public Skill() {
    }

    public Skill(String name, String imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
