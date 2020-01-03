package dev.pl.clouddietapp.models;

import java.util.Objects;

public class DatabasePhoto {
    private String id;
    private String s3PhotoId;
    private String recipeId;
    private String owner;

    public DatabasePhoto(String id, String s3PhotoId, String recipeId, String owner) {
        this.id = id;
        this.s3PhotoId = s3PhotoId;
        this.recipeId = recipeId;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getS3PhotoId() {
        return s3PhotoId;
    }

    public void setS3PhotoId(String s3PhotoId) {
        this.s3PhotoId = s3PhotoId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabasePhoto that = (DatabasePhoto) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DatabasePhoto{" +
                "id='" + id + '\'' +
                ", s3PhotoId='" + s3PhotoId + '\'' +
                ", recipeId='" + recipeId + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}
