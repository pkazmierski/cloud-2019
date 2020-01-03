package dev.pl.clouddietapp.data;

import android.content.Context;
import android.util.Log;

import com.amazonaws.amplify.generated.graphql.CreateRecipeMutation;
import com.amazonaws.amplify.generated.graphql.CreateUserDataMutation;
import com.amazonaws.amplify.generated.graphql.DeleteRecipePhotoMutation;
import com.amazonaws.amplify.generated.graphql.DeleteUserDataMutation;
import com.amazonaws.amplify.generated.graphql.GetRecipeQuery;
import com.amazonaws.amplify.generated.graphql.GetUserDataQuery;
import com.amazonaws.amplify.generated.graphql.ListRecipePhotosQuery;
import com.amazonaws.amplify.generated.graphql.ListRecipesQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserDataMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.fetcher.ResponseFetcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.models.DatabasePhoto;
import dev.pl.clouddietapp.models.Gender;
import dev.pl.clouddietapp.models.Recipe;
import dev.pl.clouddietapp.models.RecipeType;
import dev.pl.clouddietapp.models.UserData;
import dev.pl.clouddietapp.models.UserPreferences;
import type.CreateRecipeInput;
import type.CreateUserDataInput;
import type.DeleteRecipePhotoInput;
import type.DeleteUserDataInput;
import type.ModelIDFilterInput;
import type.ModelIntFilterInput;
import type.ModelRecipeFilterInput;
import type.ModelRecipePhotoFilterInput;
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
                DataStore.getUserData().getPreferences(),
                DataStore.getUserData().getRecommendedRecipes()
        ));
    }

    public void getUserAttributes(Runnable onSuccess, Runnable onFailure, Context ctx) {
        Callback<Map<String, String>> userAttributesCallback = new Callback<Map<String, String>>() {
            @Override
            public void onResult(Map<String, String> result) {
                Log.d(TAG, "userAttributesCallback onResult: " + result.toString());

                userAttributes = result;
                parseUserAttributes(ctx);
                if (onSuccess != null)
                    onSuccess.run();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: " + e.getLocalizedMessage());
                e.printStackTrace();
                if (onFailure != null)
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
                if (response.hasErrors()) {
                    Log.e(TAG, "getUserData: " + response.errors());
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

                if(response.data() == null) {
                    Log.e("getUserData", "respone.data() is null");
                    if(onFailure != null)
                        onFailure.run();
                    return;
                }

                if(response.data().getUserData() == null) {
                    Log.e("getUserData", "respone.data().getUserData() is null");
                    if(onFailure != null)
                        onFailure.run();
                    return;
                }

                //DataStore.getUserData().setFridgeContents(parseFridgeContents(response.data()));
                if (response.data().getUserData().maxDistanceToSupermarket() == null)
                    DataStore.getUserData().setPreferences(new UserPreferences(10));
                else
                    DataStore.getUserData().setPreferences(new UserPreferences(response.data().getUserData().maxDistanceToSupermarket()));

                List<Recipe> recommendedRecipes = new ArrayList<>();
                Recipe rec1 = new Recipe();
                Recipe rec2 = new Recipe();
                Recipe rec3 = new Recipe();
                Recipe rec4 = new Recipe();
                Recipe rec5 = new Recipe();
                recommendedRecipes.add(rec1);
                recommendedRecipes.add(rec2);
                recommendedRecipes.add(rec3);
                recommendedRecipes.add(rec4);
                recommendedRecipes.add(rec5);


                if (response.data().getUserData().recommendedDishes() != null) {
                    List<String> recDishes = response.data().getUserData().recommendedDishes();
                    ResponseFetcher responseFetcher = response.fromCache() ? AppSyncResponseFetchers.CACHE_ONLY : AppSyncResponseFetchers.NETWORK_ONLY;
                    for (int i = 0; i < 4; i++) {
                        getRecipeById(null, null, recDishes.get(i), recommendedRecipes.get(i), responseFetcher);
                    }

                    Runnable setRec = () -> {//final setrec
                        boolean cnt = true;
                        while(cnt) {
                            cnt = false;
                            for (Recipe r : recommendedRecipes) {
                                if(r.getId() == null || r.getId().isEmpty())
                                    cnt = true;
                            }
                        }
                        DataStore.getUserData().setRecommendedRecipes(recommendedRecipes);
//                        Log.d(TAG, "fromcache: " + response.fromCache());
                        if (onSuccess != null)
                            onSuccess.run();
                    };

                    getRecipeById(setRec, null, recDishes.get(4), rec5, responseFetcher);

                } else {
                    if (onSuccess != null)
                        onSuccess.run();
                }
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

    public void updateUserData(final Runnable onSuccess, final Runnable onFailure, UserData newUserData) {
        GraphQLCall.Callback<UpdateUserDataMutation.Data> createUserDataMutationCallback = new GraphQLCall.Callback<UpdateUserDataMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<UpdateUserDataMutation.Data> response) {


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

        List<String> reccomendedRecipesIds = null;
        if (newUserData.getRecommendedRecipes() != null) {
            reccomendedRecipesIds = new ArrayList<>();

            for (Recipe rec : newUserData.getRecommendedRecipes()) {
                reccomendedRecipesIds.add(rec.getId());
            }
        }

        UpdateUserDataInput createUserDataInput = UpdateUserDataInput.builder()
                .id(DataStore.getUserData().getUsername())
                .recommendedDishes(reccomendedRecipesIds)
                .maxDistanceToSupermarket(DataStore.getUserData().getPreferences().getMaxSupermarketDistance())
                .build();

        appSyncClient.mutate(UpdateUserDataMutation.builder()
                .input(createUserDataInput)
                .build())
                .enqueue(createUserDataMutationCallback);
    }

    public void deleteUserData(final Runnable onSuccess, final Runnable onFailure) {
        GraphQLCall.Callback<DeleteUserDataMutation.Data> deleteUserDataCallback = new GraphQLCall.Callback<DeleteUserDataMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<DeleteUserDataMutation.Data> response) {
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

        DeleteUserDataInput deleteUserDataInput = DeleteUserDataInput.builder().id(DataStore.getUserData().getUsername()).build();

        appSyncClient.mutate(DeleteUserDataMutation.builder()
                .input(deleteUserDataInput)
                .build())
                .enqueue(deleteUserDataCallback);
    }

    private ArrayList<Recipe> parseRecipes(List<ListRecipesQuery.Item> dbRecipes) {
        ArrayList<Recipe> parsedRecipes = new ArrayList<>();
        for (ListRecipesQuery.Item dbRecipe : dbRecipes) {
            Recipe recipe = new Recipe(dbRecipe.id(), dbRecipe.name(), dbRecipe.content(), RecipeType.valueOf(dbRecipe.type()), dbRecipe.calories());
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

    public void getFilteredRecipes(final Runnable onSuccess, final Runnable onFailure, final Runnable onEmptyArrayReceived, final RecipeType type, final int calories, final List<Recipe> filteredRecipeStorage) {
        GraphQLCall.Callback<ListRecipesQuery.Data> listRecipesQueryCallback = new GraphQLCall.Callback<ListRecipesQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListRecipesQuery.Data> response) {
                assert response.data() != null;
                assert response.data().listRecipes() != null;
                if(response.data().listRecipes().items().size() == 0) {
                    Log.d("getFilteredRecipes", type.toString() + ": no items");
                    onEmptyArrayReceived.run();
                }

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

    public void getRecipeById(final Runnable onSuccess, final Runnable onFailure, final String recipeId, final Recipe returnRecipe, ResponseFetcher responseFetcher) {
        GraphQLCall.Callback<GetRecipeQuery.Data> listRecipesQueryCallback = new GraphQLCall.Callback<GetRecipeQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<GetRecipeQuery.Data> response) {
                if (response.hasErrors()) {
                    Log.e(TAG, "getRecipeById: " + response.errors());
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }
                if(response.data() == null) {
                    Log.e(TAG, "getRecipeById: " + response.toString());
                    Log.e(TAG, "getRecipeById errors: " + response.errors());
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }
                if (response.data().getRecipe() == null) {
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

//                Log.d(TAG, "getRecipeById: " + response.data().getRecipe());

                GetRecipeQuery.GetRecipe dbRec = response.data().getRecipe();

                returnRecipe.setId(dbRec.id());
                returnRecipe.setName(dbRec.name());
                returnRecipe.setContent(dbRec.content());
                returnRecipe.setCalories(dbRec.calories());
                returnRecipe.setType(RecipeType.valueOf(dbRec.type()));

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

//        if(recipeIds.size() != 5) {
//            Log.e(TAG, "not enough recipe ids");
//        }
//
//        ModelRecipeFilterInput modelRecipeFilterInput1 = ModelRecipeFilterInput.builder()
//                .id(ModelIDFilterInput.builder().eq(recipeIds.get(1)).build())
//                .build();
//
//        ModelRecipeFilterInput modelRecipeFilterInput2 = ModelRecipeFilterInput.builder()
//                .id(ModelIDFilterInput.builder().eq(recipeIds.get(2)).build())
//                .build();
//
//        ModelRecipeFilterInput modelRecipeFilterInput3 = ModelRecipeFilterInput.builder()
//                .id(ModelIDFilterInput.builder().eq(recipeIds.get(3)).build())
//                .build();
//
//        ModelRecipeFilterInput modelRecipeFilterInput4 = ModelRecipeFilterInput.builder()
//                .id(ModelIDFilterInput.builder().eq(recipeIds.get(4)).build())
//                .build();
//
//        List<ModelRecipeFilterInput> modelRecipeFilterInputs = new ArrayList<>();
//        modelRecipeFilterInputs.add(modelRecipeFilterInput1);
//        modelRecipeFilterInputs.add(modelRecipeFilterInput2);
//        modelRecipeFilterInputs.add(modelRecipeFilterInput3);
//        modelRecipeFilterInputs.add(modelRecipeFilterInput4);

        if(recipeId == null || recipeId.isEmpty()) {
            Log.e(TAG, "recipeId is empty in getRecipeById");
            return;
        }

        appSyncClient.query(GetRecipeQuery.builder()
                .id(recipeId)
                .build())
                .responseFetcher(responseFetcher)
                .enqueue(listRecipesQueryCallback);
    }

    public void getPhotosForRecipe(final Runnable onSuccess, final Runnable onFailure, final String recipeId, final List<String> photoIds) {
        getPhotosForRecipe(onSuccess, onFailure, recipeId, photoIds, AppSyncResponseFetchers.CACHE_AND_NETWORK);
    }

    public void getPhotosForRecipe(final Runnable onSuccess, final Runnable onFailure, final String recipeId, final List<String> photoIds, ResponseFetcher responseFetcher) {
        if(photoIds == null) {
            Log.e(TAG, "photoIds is null in ListRecipePhotosQuery");
            return;
        }

        GraphQLCall.Callback<ListRecipePhotosQuery.Data> listRecipePhotosCallback = new GraphQLCall.Callback<ListRecipePhotosQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListRecipePhotosQuery.Data> response) {
                if (response.hasErrors()) {
                    Log.e(TAG, "ListRecipePhotosQuery: " + response.errors());
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

                if(response.data() == null) {
                    Log.e(TAG, "ListRecipePhotosQuery: data() is null");
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

                if (response.data().listRecipePhotos() == null) {
                    Log.e(TAG, "ListRecipePhotosQuery: listRecipePhotos() is null");
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

                if (response.data().listRecipePhotos().items() == null) {
                    Log.e(TAG, "ListRecipePhotosQuery: response.data().listRecipePhotos().items() is null");
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

                if(response.data().listRecipePhotos().items().size() == 0) {
                    Log.e(TAG, "ListRecipePhotosQuery: response.data().listRecipePhotos().items() is empty");
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

                photoIds.clear();
                for (ListRecipePhotosQuery.Item item : response.data().listRecipePhotos().items()) {
                    photoIds.add(item.storagePhotoId());
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

        if(recipeId == null || recipeId.isEmpty()) {
            Log.e(TAG, "recipeId is empty in ListRecipePhotosQuery");
            return;
        }

        ModelIDFilterInput modelIDFilterInput = ModelIDFilterInput.builder().eq(recipeId).build();
        ModelRecipePhotoFilterInput modelRecipePhotoFilterInput = ModelRecipePhotoFilterInput.builder().recipeId(modelIDFilterInput).build();

        appSyncClient.query(ListRecipePhotosQuery.builder()
                .filter(modelRecipePhotoFilterInput)
                .build())
                .responseFetcher(responseFetcher)
                .enqueue(listRecipePhotosCallback);
    }

    public void getPhotosForLoggedInUser(final Runnable onSuccess, final Runnable onFailure, final List<DatabasePhoto> databasePhotos) {
        final int methodNameLength = new Object() {}.getClass().getEnclosingMethod().getName().length();
        final String methodName = new Object() {}.getClass().getEnclosingMethod().getName().substring(0, methodNameLength < 23 ? methodNameLength : 22);

        if(DataStore.getUserData().getUsername() == null || DataStore.getUserData().getUsername().isEmpty()) {
            Log.e(methodName, "username is null or empty");
            return;
        }

        GraphQLCall.Callback<ListRecipePhotosQuery.Data> listRecipePhotosCallback = new GraphQLCall.Callback<ListRecipePhotosQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListRecipePhotosQuery.Data> response) {
                if (response.hasErrors()) {
                    Log.e(methodName, response.errors().toString());
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

                if(response.data() == null) {
                    Log.e(methodName, "data() is null");
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

                if (response.data().listRecipePhotos() == null) {
                    Log.e(methodName, "listRecipePhotos() is null");
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

                if (response.data().listRecipePhotos().items() == null) {
                    Log.e(methodName, "response.data().listRecipePhotos().items() is null");
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }

                if(response.data().listRecipePhotos().items().size() == 0) { //user doesn't have any photos
                    Log.d(methodName, "response.data().listRecipePhotos().items() is empty");
                    if (onSuccess != null)
                        onSuccess.run();
                    return;
                }

                databasePhotos.clear();
                for (ListRecipePhotosQuery.Item item : response.data().listRecipePhotos().items()) {
                    databasePhotos.add(new DatabasePhoto(item.id(), item.storagePhotoId(), item.recipeId(), item.owner()));
                }

                if (onSuccess != null)
                    onSuccess.run();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e("methodName", "Error: " + e.toString());
                if (onFailure != null)
                    onFailure.run();
            }
        };

        ModelStringFilterInput modelStringFilterInput = ModelStringFilterInput.builder().eq(DataStore.getUserData().getUsername()).build();
        ModelRecipePhotoFilterInput modelRecipePhotoFilterInput = ModelRecipePhotoFilterInput.builder().owner(modelStringFilterInput).build();

        appSyncClient.query(ListRecipePhotosQuery.builder()
                .filter(modelRecipePhotoFilterInput)
                .build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(listRecipePhotosCallback);
    }

    public void deletePhotoFromDynamo(final Runnable onSuccess, final Runnable onFailure, final String dynamoPhotoId) {
        GraphQLCall.Callback<DeleteRecipePhotoMutation.Data> listRecipesQueryCallback = new GraphQLCall.Callback<DeleteRecipePhotoMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<DeleteRecipePhotoMutation.Data> response) {
                if (response.hasErrors()) {
                    Log.e(TAG, "deletePhotoFromDynamo: " + response.errors());
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }
                if(response.data() == null) {
                    Log.e(TAG, "deletePhotoFromDynamo: " + response.toString());
                    if (onFailure != null)
                        onFailure.run();
                    return;
                }
                if (response.data().deleteRecipePhoto() == null) {
                    if (onFailure != null)
                        onFailure.run();
                    return;
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


        if(dynamoPhotoId == null || dynamoPhotoId.isEmpty()) {
            Log.e(TAG, "photoId is empty or null in deletePhotoFromDynamo");
            return;
        }

        DeleteRecipePhotoInput deleteRecipeInput = DeleteRecipePhotoInput.builder().id(dynamoPhotoId).build();

        appSyncClient.mutate(DeleteRecipePhotoMutation.builder()
                .input(deleteRecipeInput)
                .build())
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

    public void createNewRecipe(Recipe recipe) {
        GraphQLCall.Callback<CreateRecipeMutation.Data> createRecipeCallback = new GraphQLCall.Callback<CreateRecipeMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<CreateRecipeMutation.Data> response) {
                if (response.hasErrors())
                    Log.e(TAG, "createNewRecipe: " + response.errors());
                else if (response.data().createRecipe() != null)
                    Log.d(TAG, "createNewRecipe success: " + response.data().toString());
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e("ERROR", e.toString());
            }
        };

        CreateRecipeInput createRecipeInput = CreateRecipeInput.builder()
                .name(recipe.getName())
                .content(recipe.getContent())
                .calories(recipe.getCalories())
                .type(recipe.getType().toString())
                .build();

        appSyncClient.mutate(CreateRecipeMutation.builder()
                .input(createRecipeInput)
                .build())
                .enqueue(createRecipeCallback);
    }
}
