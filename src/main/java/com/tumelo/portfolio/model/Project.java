package com.tumelo.portfolio.model;

public class Project {

    private String title;
    private String description;
    private String imageId;
    private String link;

    public Project() {
    }

    public Project(
            String title,
            String description,
            String imageId,
            String link
    ) {
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
