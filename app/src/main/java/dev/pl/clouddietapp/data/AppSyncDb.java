package dev.pl.clouddietapp.data;

import android.util.Log;

import com.amazonaws.amplify.generated.graphql.CreateUserDataMutation;
import com.amazonaws.amplify.generated.graphql.GetUserDataQuery;
import com.amazonaws.amplify.generated.graphql.ListRecipesQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserDataMutation;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import dev.pl.clouddietapp.models.Food;
import dev.pl.clouddietapp.models.Recipe;
import dev.pl.clouddietapp.models.UserData;
import dev.pl.clouddietapp.models.UserPreferences;
import type.CreateUserDataInput;
import type.UpdateUserDataInput;

import static dev.pl.clouddietapp.logic.Logic.appSyncClient;

public class AppSyncDb {
    //TODO sprawdzić, czy nie potrzeba toStringów np. w queryData.getUserData().preferences() itp.
    private static final String TAG = "AppSyncDb";
    private String recipesNextToken;

    public AppSyncDb() {
    }

    private UserPreferences parseUserPreferences(GetUserDataQuery.Data queryData) {
        try {
            //Preferences
            JSONObject jsonPreferences = new JSONObject(queryData.getUserData().preferences());
            UserPreferences userPreferences = new UserPreferences(
                    jsonPreferences.getInt("maxSupermarketDistance"),
                    jsonPreferences.getBoolean("isVegetarian"));
            Log.d(TAG, "parseUserPreferences: " + userPreferences.toString());
            return userPreferences;
        } catch (JSONException e) {
            e.printStackTrace();
            return new UserPreferences();
        }
    }

    private ArrayList<Food> parseFridgeContents(GetUserDataQuery.Data queryData) {
        ArrayList<Food> foods = new ArrayList<>();
        if (queryData.getUserData().fridgeContents() == null)
            return foods;

        try {
            //Fridge contents
            JSONObject jsonObjectFridgeContents = new JSONObject(queryData.getUserData().fridgeContents());
            JSONArray jsonArrayFridgeContents = jsonObjectFridgeContents.getJSONArray("fridgeContents");

            for (int i = 0; i < jsonArrayFridgeContents.length(); i++) {
                JSONObject jsonObjectFood = jsonArrayFridgeContents.getJSONObject(i);
                Food food = new Food(
                        jsonObjectFood.getString("name"),
                        jsonObjectFood.getDouble("amount")
                );
                foods.add(food);
            }
            Log.d(TAG, "parseFridgeContents: " + foods.toString());
            return foods;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private ArrayList<Recipe> parseRecipes(ListRecipesQuery.Data queryData) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        if (queryData.listRecipes() == null || queryData.listRecipes().items() == null)
            return recipes;

        try {
            List<ListRecipesQuery.Item> queryItems = queryData.listRecipes().items();

            for (int i = 0; i < queryItems.size(); i++) {
                ListRecipesQuery.Item currentItem = queryItems.get(i);

                //Food
                ArrayList<Food> foods = new ArrayList<>();
                JSONObject jsonObjectFoodList = new JSONObject(currentItem.food());
                JSONArray jsonArrayFood = jsonObjectFoodList.getJSONArray("foods");

                for (int j = 0; j < jsonArrayFood.length(); j++) {
                    JSONObject jsonObjectFood = jsonArrayFood.getJSONObject(j);
                    Food food = new Food(
                            jsonObjectFood.getString("name"),
                            jsonObjectFood.getDouble("amount")
                    );
                    foods.add(food);
                }

                //Content
                String content = queryItems.get(i).content();
                recipes.add(new Recipe(currentItem.id(), foods, content));
            }
            Log.d(TAG, "parseRecipes: " + recipes.toString());
            return recipes;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String fridgeContentsToJson(ArrayList<Food> fridgeContents) {
        try {
            ArrayList<Food> foods = DataStore.getUserData().getFridgeContents();
            JSONObject jsonObjectFoodsArray = new JSONObject();
            JSONArray jsonArrayFoods = new JSONArray();

            for (Food food : foods) {
                JSONObject jsonObjectFood = new JSONObject();
                jsonObjectFood.put("name", food.getName());
                jsonObjectFood.put("amount", food.getAmount());

                jsonArrayFoods.put(jsonObjectFood);
            }

            jsonObjectFoodsArray.put("fridgeContents", jsonArrayFoods);
            return jsonObjectFoodsArray.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String preferencesToJson(UserPreferences preferences) {
        try {
            UserPreferences userPreferences = DataStore.getUserData().getPreferences();
            JSONObject jsonObjectUserPreferences = new JSONObject();
            jsonObjectUserPreferences.put("maxSupermarketDistance", userPreferences.getMaxSupermarketDistance());
            jsonObjectUserPreferences.put("isVegetarian", userPreferences.isVegetarian());
            return jsonObjectUserPreferences.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getUserData(final Runnable onSuccess, final Runnable onFailure) {
        GraphQLCall.Callback<GetUserDataQuery.Data> getUserDataCallback = new GraphQLCall.Callback<GetUserDataQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<GetUserDataQuery.Data> response) {

                DataStore.getUserData().setFridgeContents(parseFridgeContents(response.data()));
                DataStore.getUserData().setPreferences(parseUserPreferences(response.data()));

                if (onSuccess != null)
                    onSuccess.run();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e("ERROR", e.toString());
                if (onFailure != null)
                    onFailure.run();
            }
        };

        appSyncClient.query(GetUserDataQuery.builder()
                .id(DataStore.getUserData().getUsername())
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(getUserDataCallback);
    }

    public void createUserData(final Runnable onSuccess, final Runnable onFailure) {
        GraphQLCall.Callback<CreateUserDataMutation.Data> createUserDataMutationCallback = new GraphQLCall.Callback<CreateUserDataMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<CreateUserDataMutation.Data> response) {

                //TODO implement

                if (onSuccess != null)
                    onSuccess.run();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e("ERROR", e.toString());
                if (onFailure != null)
                    onFailure.run();
            }
        };

        CreateUserDataInput createUserDataInput = CreateUserDataInput.builder()
                .id(DataStore.getUserData().getUsername())
                .build();

        appSyncClient.mutate(CreateUserDataMutation.builder()
                .input(createUserDataInput)
                .build())
                .enqueue(createUserDataMutationCallback);
    }

    public void setUserData(final Runnable onSuccess, final Runnable onFailure, UserData newUserData) {
        GraphQLCall.Callback<UpdateUserDataMutation.Data> createUserDataMutationCallback = new GraphQLCall.Callback<UpdateUserDataMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<UpdateUserDataMutation.Data> response) {

                DataStore.setUserData(newUserData);

                if (onSuccess != null)
                    onSuccess.run();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e("ERROR", e.toString());
                if (onFailure != null)
                    onFailure.run();
            }
        };

        UpdateUserDataInput createUserDataInput = UpdateUserDataInput.builder()
                .id(DataStore.getUserData().getUsername())
                .fridgeContents(fridgeContentsToJson(newUserData.getFridgeContents()))
                .preferences(preferencesToJson(newUserData.getPreferences()))
                .build();

        appSyncClient.mutate(UpdateUserDataMutation.builder()
                .input(createUserDataInput)
                .build())
                .enqueue(createUserDataMutationCallback);
    }

    public void getAllRecipes(final Runnable onSuccess, final Runnable onFailure) {
        GraphQLCall.Callback<ListRecipesQuery.Data> listRecipesQueryCallback = new GraphQLCall.Callback<ListRecipesQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListRecipesQuery.Data> response) {
                assert response.data() != null;
                assert response.data().listRecipes() != null;

                if (response.data().listRecipes().nextToken() != null)
                    recipesNextToken = response.data().listRecipes().nextToken();

                DataStore.setRecipes(parseRecipes(response.data()));

                if (onSuccess != null)
                    onSuccess.run();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e("ERROR", e.toString());
                if (onFailure != null)
                    onFailure.run();
            }
        };

        appSyncClient.query(ListRecipesQuery.builder()
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(listRecipesQueryCallback);
    }

    public void getAllRecipesNext(final Runnable onSuccess, final Runnable onFailure) {
        GraphQLCall.Callback<ListRecipesQuery.Data> listRecipesQueryCallback = new GraphQLCall.Callback<ListRecipesQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListRecipesQuery.Data> response) {
                DataStore.setRecipes(parseRecipes(response.data()));

                if (onSuccess != null)
                    onSuccess.run();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e("ERROR", e.toString());
                if (onFailure != null)
                    onFailure.run();
            }
        };

        appSyncClient.query(ListRecipesQuery.builder()
                .nextToken(recipesNextToken)
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(listRecipesQueryCallback);
    }
}
