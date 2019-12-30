package dev.pl.clouddietapp.logic;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider;
import com.amazonaws.regions.Regions;

import dev.pl.clouddietapp.data.AppSyncDb;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.models.Gender;
import dev.pl.clouddietapp.models.UserData;

public class Logic {
    public static AWSAppSyncClient appSyncClient;
    public static AppSyncDb appSyncDb = new AppSyncDb();

    public static void initAppSync(Context ctx) {
        appSyncClient = AWSAppSyncClient.builder()
                .context(ctx)
                .awsConfiguration(new AWSConfiguration(ctx))
                .region(Regions.EU_CENTRAL_1)
                .cognitoUserPoolsAuthProvider(new CognitoUserPoolsAuthProvider() {
                    @Override
                    public String getLatestAuthToken() {
                        try {
                            return AWSMobileClient.getInstance().getTokens().getIdToken().getTokenString();
                        } catch (Exception e){
                            Log.e("APPSYNC_ERROR", e.getLocalizedMessage());
                            return e.getLocalizedMessage();
                        }
                    }
                })
                .build();
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
