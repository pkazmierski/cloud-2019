package dev.pl.clouddietapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.ListUserDatasQuery;
import com.amazonaws.mobile.client.AWSMobileClient;

import java.util.ArrayList;
import java.util.List;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.data.AppSyncDb;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.logic.Logic;
import dev.pl.clouddietapp.models.Food;
import dev.pl.clouddietapp.models.UserData;
import dev.pl.clouddietapp.models.UserPreferences;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private List<ListUserDatasQuery.Item> userDataList = new ArrayList<>();
    private static int counter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logic.initAppSync(this);

        DataStore.getUserData().setUsername(AWSMobileClient.getInstance().getUsername());

        Logic.appSyncDb.getUserData(afterGettingUserData, null);
    }

   private Runnable afterGettingUserData = () -> runOnUiThread(() -> {
        Log.d(TAG, "got user fridge contents: " + DataStore.getUserData().getFridgeContents().toString());
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
}
