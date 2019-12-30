package dev.pl.clouddietapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.amazonaws.amplify.generated.graphql.ListUserDatasQuery;
import com.amazonaws.mobile.client.AWSMobileClient;

import java.util.ArrayList;
import java.util.List;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.logic.Logic;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private List<ListUserDatasQuery.Item> userDataList = new ArrayList<>();
    private static int counter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//inflate your activity layout here!
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_main, null, false);
        drawer.addView(contentView, 0);

        Logic.initAppSync(this);

//        addNewFoodDefinitions(milk2Percent);
//        addNewFoodDefinitions(cornFlakes);

        DataStore.getUserData().setUsername(AWSMobileClient.getInstance().getUsername());

        Logic.appSyncDb.getUserData(afterGettingUserData, null);
    }

   private Runnable afterGettingUserData = () -> runOnUiThread(() -> {
//        Log.d(TAG, "got user fridge contents: " + DataStore.getUserData().getFridgeContents().toString());
        Log.d(TAG, "got user preferences: " + DataStore.getUserData().getPreferences().toString());
//        UserData newUserData = DataStore.getUserData();
//
//        if(counter == 2) {
//            newUserData.getFridgeContents().add(new Food("Lamb", 300));
//            newUserData.getPreferences().setMaxSupermarketDistance(15);
//
//            Runnable afterSettingUserData = () -> runOnUiThread(() -> {
//                Log.d(TAG, "upload user fridge contents: " + DataStore.getUserData().getFridgeContents().toString());
//                Log.d(TAG, "upload user preferences: " + DataStore.getUserData().getPreferences().toString());
//            });
//
//            Logic.appSyncDb.setUserData(afterSettingUserData, null, newUserData);
//        }
//
//        counter++;
    });

//    private CreateFoodDefinitionInput cornFlakes = CreateFoodDefinitionInput.builder()
//            .name("Corn Flakes")
//            .unit("kg")
//            .build();
//
//    private CreateFoodDefinitionInput milk2Percent = CreateFoodDefinitionInput.builder()
//            .name("Milk 2% fat")
//            .unit("l")
//            .build();
//
//    private void addNewFoodDefinitions(CreateFoodDefinitionInput createFoodDefinitionInput) {
//        GraphQLCall.Callback<CreateFoodDefinitionMutation.Data> createFoodDefinitionMutationCallback = new GraphQLCall.Callback<CreateFoodDefinitionMutation.Data>() {
//            @Override
//            public void onResponse(@Nonnull Response<CreateFoodDefinitionMutation.Data> response) {
//                Log.d(TAG, "addnewfood: " + response.errors().toString());
//            }
//
//            @Override
//            public void onFailure(@Nonnull ApolloException e) {
//                Log.e("ERROR", e.toString());
//            }
//        };
//
//        appSyncClient.mutate(CreateFoodDefinitionMutation.builder()
//                .input(createFoodDefinitionInput)
//                .build())
//                .enqueue(createFoodDefinitionMutationCallback);
//    }
}
