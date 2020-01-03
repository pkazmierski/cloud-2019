package dev.pl.clouddietapp.models;

import java.util.List;
import java.util.Objects;

public class Recipe {
    private String id;

    //    private ArrayList<Food> foodList;
    private String name;
    private String content;
    private List<String> photos;
    private RecipeType type;
    private int calories;

//    public Recipe(String id, ArrayList<Food> foodList, String content) {
//        this.id = id;
//        this.foodList = foodList;
//        this.content = content;
//    }

    public Recipe() {

    }


    public Recipe(String id, String name, String content, List<String> photos, RecipeType type, int calories) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.photos = photos;
        this.type = type;
        this.calories = calories;
    }

    public Recipe(String id, String content, RecipeType type, int calories) {
        this.id = id;
        this.content = content;
        this.photos = null;
        this.type = type;
        this.calories = calories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public RecipeType getType() {
        return type;
    }

    public void setType(RecipeType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    //    public ArrayList<Food> getFoodList() {
//        return foodList;
//    }
//
//    public void setFoodList(ArrayList<Food> foodList) {
//        this.foodList = foodList;
//    }

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
                ", content='" + content + '\'' +
                ", photos=" + photos +
                ", type=" + type +
                ", calories=" + calories +
                '}';
    }
}
