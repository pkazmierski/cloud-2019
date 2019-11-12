package dev.pl.clouddietapp.models;

import java.util.ArrayList;
import java.util.Objects;

public class Recipe {
    private String id;
    private ArrayList<Food> foodList;
    private String content;

    public Recipe(String id, ArrayList<Food> foodList, String content) {
        this.id = id;
        this.foodList = foodList;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Food> getFoodList() {
        return foodList;
    }

    public void setFoodList(ArrayList<Food> foodList) {
        this.foodList = foodList;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return id.equals(recipe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id='" + id + '\'' +
                ", foodList=" + foodList +
                ", content='" + content + '\'' +
                '}';
    }
}
