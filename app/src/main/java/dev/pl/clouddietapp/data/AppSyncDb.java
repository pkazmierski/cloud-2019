package dev.pl.clouddietapp.data;

import android.content.Context;
import android.util.Log;

import com.amazonaws.amplify.generated.graphql.CreateUserDataMutation;
import com.amazonaws.amplify.generated.graphql.GetUserDataQuery;
import com.amazonaws.amplify.generated.graphql.ListRecipesQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserDataMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.models.Gender;
import dev.pl.clouddietapp.models.Recipe;
import dev.pl.clouddietapp.models.RecipeType;
import dev.pl.clouddietapp.models.UserData;
import dev.pl.clouddietapp.models.UserPreferences;
import type.CreateUserDataInput;
import type.ModelIntFilterInput;
import type.ModelRecipeFilterInput;
import type.ModelStringFilterInput;
import type.UpdateUserDataInput;

import static dev.pl.clouddietapp.logic.Logic.appSyncClient;

public class AppSyncDb {
    private static final String TAG = "AppSyncDb";
    private String recipesNextToken;
//    private String foodDefinitionsNextToken;

    public AppSyncDb() {
    }

    Map<String, String> userAttributes;

    private void parseUserAttributes(Context ctx) {
        String[] activityArr = ctx.getResources().getStringArray(R.array.spinnerActivity);
        int index = Arrays.asList(activityArr).indexOf(userAttributes.get("custom:physicalActivity"));

        DataStore.setUserData(new UserData(AWSMobileClient.getInstance().getUsername(),
                userAttributes.get("custom:name"),
                Integer.valueOf(userAttributes.get("custom:age")),
                Integer.valueOf(userAttributes.get("custom:height")),
                Double.valueOf(userAttributes.get("custom:weight")),
                index,
                userAttributes.get("custom:gender").equals("1") ? Gender.MALE : Gender.FEMALE,
                userAttributes.get("custom:location"),
                null
        ));
    }

    public void getUserAttributes(Runnable onSuccess, Runnable onFailure, Context ctx) {
        Callback<Map<String, String>> userAttributesCallback = new Callback<Map<String, String>>() {
            @Override
            public void onResult(Map<String, String> result) {
                Log.d(TAG, "userAttributesCallback onResult: " + result.toString());

                userAttributes = result;
                parseUserAttributes(ctx);
                onSuccess.run();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: " + e.getLocalizedMessage());
                e.printStackTrace();
                onFailure.run();
            }

        };
        AWSMobileClient.getInstance().getUserAttributes(userAttributesCallback);
    }

    /*private UserPreferences parseUserPreferences(GetUserDataQuery.Data queryData) {
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
    }*/

    /*private ArrayList<Food> parseFridgeContents(GetUserDataQuery.Data queryData) {
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
                        jsonObjectFood.getString("id"),
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
    }*/

    /*private ArrayList<FoodDefinition> parseFoodDefinitons(ListFoodDefinitionsQuery.Data queryData) {
        ArrayList<FoodDefinition> foodDefinitions = new ArrayList<>();
        if (queryData.listFoodDefinitions() == null || queryData.listFoodDefinitions().items() == null)
            return foodDefinitions;


        List<ListFoodDefinitionsQuery.Item> foodDefinitionsListQuery = queryData.listFoodDefinitions().items();
        Log.d(TAG, "foodDefinitionsListQuery: " + foodDefinitionsListQuery.toString());

        for (int i = 0; i < foodDefinitionsListQuery.size(); i++) {
            ListFoodDefinitionsQuery.Item currentDefinition = foodDefinitionsListQuery.get(i);
            FoodDefinition newFoodDefinition = new FoodDefinition(
                    currentDefinition.id(),
                    currentDefinition.name(),
                    currentDefinition.unit());
            foodDefinitions.add(newFoodDefinition);
        }
        Log.d(TAG, "parseFoodDefinitons: " + foodDefinitions.toString());
        return foodDefinitions;
    }*/

    /*private ArrayList<Recipe> parseRecipes(ListRecipesQuery.Data queryData) {
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
    }*/

    /*private String fridgeContentsToJson(ArrayList<Food> fridgeContents) {
        try {
            ArrayList<Food> foods = DataStore.getUserData().getFridgeContents();
            JSONObject jsonObjectFoodsArray = new JSONObject();
            JSONArray jsonArrayFoods = new JSONArray();

            for (Food food : foods) {
                JSONObject jsonObjectFood = new JSONObject();
                jsonObjectFood.put("id", food.getId());
                jsonObjectFood.put("amount", food.getAmount());

                jsonArrayFoods.put(jsonObjectFood);
            }

            jsonObjectFoodsArray.put("fridgeContents", jsonArrayFoods);
            return jsonObjectFoodsArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }*/

    /*private String preferencesToJson(UserPreferences preferences) {
        try {
            UserPreferences userPreferences = DataStore.getUserData().getPreferences();
            JSONObject jsonObjectUserPreferences = new JSONObject();
            jsonObjectUserPreferences.put("maxSupermarketDistance", userPreferences.getMaxSupermarketDistance());
            jsonObjectUserPreferences.put("isVegetarian", userPreferences.isVegetarian());
            return jsonObjectUserPreferences.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }*/

    /*public void getFoodDefinitions(final Runnable onSuccess, final Runnable onFailure) {
        GraphQLCall.Callback<ListFoodDefinitionsQuery.Data> listFoodDefinitionsQueryCallback = new GraphQLCall.Callback<ListFoodDefinitionsQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListFoodDefinitionsQuery.Data> response) {
                assert response.data() != null;

                if (response.data().listFoodDefinitions().nextToken() != null)
                    foodDefinitionsNextToken = response.data().listFoodDefinitions().nextToken();

                DataStore.setFoodDefinitions(parseFoodDefinitons(response.data()));

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

        appSyncClient.query(ListFoodDefinitionsQuery.builder()
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(listFoodDefinitionsQueryCallback);
    }*/

    public void getUserData(final Runnable onSuccess, final Runnable onFailure) {
        GraphQLCall.Callback<GetUserDataQuery.Data> getUserDataCallback = new GraphQLCall.Callback<GetUserDataQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<GetUserDataQuery.Data> response) {

//                DataStore.getUserData().setFridgeContents(parseFridgeContents(response.data()));
                if (response.data().getUserData().maxDistanceToSupermarket() == null) {
                    DataStore.getUserData().setPreferences(new UserPreferences(10));
                } else {
                    DataStore.getUserData().setPreferences(new UserPreferences(response.data().getUserData().maxDistanceToSupermarket()));
                }

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
//                .fridgeContents(fridgeContentsToJson(newUserData.getFridgeContents()))
                .maxDistanceToSupermarket(DataStore.getUserData().getPreferences().getMaxSupermarketDistance())
                .build();

        appSyncClient.mutate(UpdateUserDataMutation.builder()
                .input(createUserDataInput)
                .build())
                .enqueue(createUserDataMutationCallback);
    }

    private ArrayList<Recipe> parseRecipes(List<ListRecipesQuery.Item> dbRecipes) {
        ArrayList<Recipe> parsedRecipes = new ArrayList<>();
        for (ListRecipesQuery.Item dbRecipe : dbRecipes) {
            Recipe recipe = new Recipe(dbRecipe.id(), dbRecipe.content(), dbRecipe.photo(), RecipeType.valueOf(dbRecipe.type()), dbRecipe.calories());
            parsedRecipes.add(recipe);
        }
        return parsedRecipes;
    }

    public void getAllRecipes(final Runnable onSuccess, final Runnable onFailure) {
        GraphQLCall.Callback<ListRecipesQuery.Data> listRecipesQueryCallback = new GraphQLCall.Callback<ListRecipesQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListRecipesQuery.Data> response) {
                assert response.data() != null;
                assert response.data().listRecipes() != null;

                if (response.data().listRecipes().nextToken() != null)
                    recipesNextToken = response.data().listRecipes().nextToken();

                DataStore.setRecipes(parseRecipes(Objects.requireNonNull(Objects.requireNonNull(response.data().listRecipes()).items())));

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

    public void getFilteredRecipes(final Runnable onSuccess, final Runnable onFailure, final RecipeType type, final int calories, final List<Recipe> filteredRecipeStorage) {
        GraphQLCall.Callback<ListRecipesQuery.Data> listRecipesQueryCallback = new GraphQLCall.Callback<ListRecipesQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListRecipesQuery.Data> response) {
                assert response.data() != null;
                assert response.data().listRecipes() != null;

                filteredRecipeStorage.clear();
                filteredRecipeStorage.addAll(parseRecipes(Objects.requireNonNull(Objects.requireNonNull(response.data().listRecipes()).items())));

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

        ModelRecipeFilterInput modelRecipeFilterInput = ModelRecipeFilterInput.builder()
                .calories(ModelIntFilterInput.builder().ge(calories - 50).lt(calories + 50).build())
                .type(ModelStringFilterInput.builder().eq(type.toString()).build())
                .build();

        appSyncClient.query(ListRecipesQuery.builder()
                .filter(modelRecipeFilterInput)
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(listRecipesQueryCallback);
    }

//    public void getAllRecipesNext(final Runnable onSuccess, final Runnable onFailure) {
//        GraphQLCall.Callback<ListRecipesQuery.Data> listRecipesQueryCallback = new GraphQLCall.Callback<ListRecipesQuery.Data>() {
//            @Override
//            public void onResponse(@Nonnull Response<ListRecipesQuery.Data> response) {
//                DataStore.setRecipes(parseRecipes(response.data()));
//
//                if (onSuccess != null)
//                    onSuccess.run();
//            }
//
//            @Override
//            public void onFailure(@Nonnull ApolloException e) {
//                Log.e("ERROR", e.toString());
//                if (onFailure != null)
//                    onFailure.run();
//            }
//        };
//
//        appSyncClient.query(ListRecipesQuery.builder()
//                .nextToken(recipesNextToken)
//                .build())
//                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
//                .enqueue(listRecipesQueryCallback);
//    }
}
