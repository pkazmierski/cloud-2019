package dev.pl.clouddietapp.logic;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider;
import com.amazonaws.regions.Regions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.pl.clouddietapp.data.AppSyncDb;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.models.Gender;
import dev.pl.clouddietapp.models.Recipe;
import dev.pl.clouddietapp.models.RecipeType;
import dev.pl.clouddietapp.models.UserData;

public class Logic {
    public static AWSAppSyncClient appSyncClient;
    public static AppSyncDb appSyncDb = new AppSyncDb();
    private static boolean init = false;

    public static void initAppSync(Context ctx) {
        if (!init) {
            appSyncClient = AWSAppSyncClient.builder()
                    .context(ctx)
                    .awsConfiguration(new AWSConfiguration(ctx))
                    .region(Regions.EU_CENTRAL_1)
                    .cognitoUserPoolsAuthProvider(new CognitoUserPoolsAuthProvider() {
                        @Override
                        public String getLatestAuthToken() {
                            try {
                                return AWSMobileClient.getInstance().getTokens().getIdToken().getTokenString();
                            } catch (Exception e) {
                                Log.e("APPSYNC_ERROR", e.getLocalizedMessage());
                                return e.getLocalizedMessage();
                            }
                        }
                    })
                    .build();
            init = true;
        }
    }

    public static List<Recipe> recommendRecipes(List<Recipe> filteredRecipes) {
        List<Recipe> toReturn = new ArrayList<>();
        List<Recipe> temp = new ArrayList<>();
        Random rand = new Random();

        Recipe breakfast;
        for (Recipe r : filteredRecipes) {
            if (r.getType() == RecipeType.BREAKFAST)
                temp.add(r);
        }
        breakfast = temp.get(rand.nextInt(temp.size()));

        temp.clear();
        Recipe secondBreakfast;
        for (Recipe r : filteredRecipes) {
            if (r.getType() == RecipeType.SECOND_BREAKFAST)
                temp.add(r);
        }
        secondBreakfast = temp.get(rand.nextInt(temp.size()));

        temp.clear();
        Recipe dinner;
        for (Recipe r : filteredRecipes) {
            if (r.getType() == RecipeType.DINNER)
                temp.add(r);
        }
        dinner = temp.get(rand.nextInt(temp.size()));


        temp.clear();
        Recipe afterDinner;
        for (Recipe r : filteredRecipes) {
            if (r.getType() == RecipeType.AFTER_DINNER)
                temp.add(r);
        }
        afterDinner = temp.get(rand.nextInt(temp.size()));


        temp.clear();
        Recipe supper;
        for (Recipe r : filteredRecipes) {
            if (r.getType() == RecipeType.SUPPER)
                temp.add(r);
        }
        supper = temp.get(rand.nextInt(temp.size()));


        toReturn.add(breakfast);
        toReturn.add(secondBreakfast);
        toReturn.add(dinner);
        toReturn.add(afterDinner);
        toReturn.add(supper);

        return toReturn;
    }

    public static double calculateBMR() {
        double bmr = 0;
        UserData ud = DataStore.getUserData();

        if (ud.getGender() == Gender.MALE)
            bmr = 66.47 + (13.75 * ud.getWeight()) + (5.003 * ud.getHeightInCm()) - (6.755 * ud.getAge());
        else
            bmr = 655.1 + (9.563 * ud.getWeight()) + (1.85 * ud.getHeightInCm()) - (4.676 * ud.getAge());

        switch (ud.getPhysicalActivity()) {
            case 0:
                bmr *= 1.2;
                break;
            case 1:
                bmr *= 1.375;
                break;
            case 2:
                bmr *= 1.55;
                break;
            case 3:
                bmr *= 1.725;
                break;
            case 4:
                bmr *= 1.9;
                break;
        }

        return bmr;
    }

}
