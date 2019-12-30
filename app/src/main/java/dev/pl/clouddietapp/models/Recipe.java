package dev.pl.clouddietapp.models;

import java.util.List;
import java.util.Objects;

public class Recipe {
    private String id;
//    private ArrayList<Food> foodList;
    private String content;
    private List<String> photo;
    private RecipeType type;
    private int calories;

//    public Recipe(String id, ArrayList<Food> foodList, String content) {
//        this.id = id;
//        this.foodList = foodList;
//        this.content = content;
//    }


    public Recipe(String id, String content, List<String> photo, RecipeType type, int calories) {
        this.id = id;
        this.content = content;
        this.photo = photo;
        this.type = type;
        this.calories = calories;
    }

    public List<String> getPhoto() {
        return photo;
    }

    public void setPhoto(List<String> photo) {
        this.photo = photo;
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
                ", photo=" + photo +
                ", type=" + type +
                ", calories=" + calories +
                '}';
    }
}
