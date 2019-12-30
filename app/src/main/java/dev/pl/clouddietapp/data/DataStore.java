package dev.pl.clouddietapp.data;

import java.util.ArrayList;

import dev.pl.clouddietapp.models.FoodDefinition;
import dev.pl.clouddietapp.models.Recipe;
import dev.pl.clouddietapp.models.UserData;

public class DataStore {
    private static UserData userData = new UserData();
    private static ArrayList<Recipe> recipes = new ArrayList<>();
    private static ArrayList<FoodDefinition> foodDefinitions = new ArrayList<>();

    public static UserData getUserData() {
        return userData;
    }

    public static void setUserData(UserData userData) {
        DataStore.userData = userData;
    }

    public static ArrayList<Recipe> getRecipes() {
        return recipes;
    }

    public static void setRecipes(ArrayList<Recipe> recipes) {
        DataStore.recipes.clear();
        DataStore.recipes.addAll(recipes);
    }

    public static ArrayList<FoodDefinition> getFoodDefinitions() {
        return foodDefinitions;
    }

    public static void setFoodDefinitions(ArrayList<FoodDefinition> foodDefinitions) {
        DataStore.foodDefinitions.clear();
        DataStore.foodDefinitions.addAll(foodDefinitions);
    }

    public static FoodDefinition getFoodDefinitionById(String foodId) {
        for(FoodDefinition foodDefinition : foodDefinitions) {
            if(foodDefinition.getId().equals(foodId))
                return foodDefinition;
        }
        return null;
    }
}
