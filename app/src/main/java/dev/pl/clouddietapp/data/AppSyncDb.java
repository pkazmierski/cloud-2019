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

import java.util.ArrayList;

import javax.annotation.Nonnull;

import dev.pl.clouddietapp.models.Food;
import dev.pl.clouddietapp.models.Recipe;
import dev.pl.clouddietapp.models.UserData;
import type.CreateUserDataInput;
import type.UpdateUserDataInput;

import static dev.pl.clouddietapp.logic.Logic.appSyncClient;

public class AppSyncDb {
    private String recipesNextToken;

    public AppSyncDb() {
    }

    private UserData parseUserData(GetUserDataQuery.Data queryData) {
        //todo implement parseUserData
        return null;
    }

    private ArrayList<Recipe> parseRecipes(ListRecipesQuery.Data queryData) {
        //todo implement parseRecipes
        return null;
    }

    private String fridgeContentsToJson(ArrayList<Food> fridgeContents) {
        //todo implement fridgeContentsToJson
        return null;
    }

    private String preferencesToJson(UserData.Preferences preferences) {
        //todo implement preferencesToJson
        return null;
    }

    public void getUserData(final Runnable onSuccess, final Runnable onFailure) {
        GraphQLCall.Callback<GetUserDataQuery.Data> getUserDataCallback = new GraphQLCall.Callback<GetUserDataQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<GetUserDataQuery.Data> response) {
                DataStore.setUserData(parseUserData(response.data()));

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

    public void setUserData(final Runnable onSuccess, final Runnable onFailure) {
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

        UpdateUserDataInput createUserDataInput = UpdateUserDataInput.builder()
                .id(DataStore.getUserData().getUsername())
                .fridgeContents(fridgeContentsToJson(DataStore.getUserData().getFridgeContents()))
                .preferences(preferencesToJson(DataStore.getUserData().getPreferences()))
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

                if(response.data().listRecipes().nextToken() != null)
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
