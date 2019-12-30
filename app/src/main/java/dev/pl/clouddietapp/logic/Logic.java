package dev.pl.clouddietapp.logic;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider;
import com.amazonaws.regions.Regions;


import java.text.DateFormat;
import java.text.SimpleDateFormat;

import dev.pl.clouddietapp.data.AppSyncDb;

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
}
