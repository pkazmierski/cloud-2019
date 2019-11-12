package dev.pl.clouddietapp.data;

import java.util.ArrayList;

import dev.pl.clouddietapp.models.Recipe;
import dev.pl.clouddietapp.models.UserData;

public class DataStore {
    private static UserData userData = new UserData();
    private static ArrayList<Recipe> recipes = new ArrayList<>();

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
        DataStore.recipes = recipes;
    }
}
