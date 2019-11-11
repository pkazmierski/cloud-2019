package dev.pl.clouddietapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.CreateUserDataMutation;
import com.amazonaws.amplify.generated.graphql.GetUserDataQuery;
import com.amazonaws.amplify.generated.graphql.ListUserDatasQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserDataMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import dev.pl.clouddietapp.R;
import type.CreateUserDataInput;
import type.UpdateUserDataInput;

public class MainActivity extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;

    private String privateData;
    private String preferences;
    private String dietData;

    private EditText privateDataText;
    private EditText preferencesText;
    private EditText dietDataText;

    private TextView privateDataTextView;
    private TextView preferencesTextView;
    private TextView dietDataTextView;

    private String settingObjectId;

    private List<ListUserDatasQuery.Item> userDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        privateDataText = (EditText) findViewById(R.id.privateDataText);
        preferencesText = (EditText) findViewById(R.id.preferencesText);
        dietDataText = (EditText) findViewById(R.id.dietDataText);

        privateDataTextView = (TextView) findViewById(R.id.privateDataTextView);
        preferencesTextView = (TextView) findViewById(R.id.preferencesTextView);
        dietDataTextView = (TextView) findViewById(R.id.dietDataTextView);

        initializeAppSync();

        queryDatabase(this.findViewById(android.R.id.content));

        if (privateData != null) { //TODO dac jakas zmienna static po rejestracji i ja zczytac tutaj
            createUserDataMutation();
        }
    }

    public void queryDatabase(View view) {
        userDataListQuery();
    }

    public void updateDatabase(View view) {
        privateData = privateDataText.getText().toString();
        preferences = preferencesText.getText().toString();
        dietData = dietDataText.getText().toString();

        updateUserDataMutation(privateData, preferences, dietData);
    }

    private void refreshText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                privateDataText.setText(privateData);
                preferencesText.setText(preferences);
                dietDataText.setText(dietData);

                privateDataTextView.setText(privateData);
                preferencesTextView.setText(preferences);
                dietDataTextView.setText(dietData);
            }
        });
    }

    private void initializeAppSync() {
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
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
                }).build();
    }

    private void createUserDataMutation() {
        CreateUserDataInput ModelUserDataFilterInput = CreateUserDataInput.builder()
                .privateData("test1")
                .preferences("test2")
                .dietData("test3")
                .build();

        mAWSAppSyncClient.mutate(CreateUserDataMutation.builder().input(ModelUserDataFilterInput).build())
                .enqueue(createUserDataMutationCallback);
    }

    private GraphQLCall.Callback<CreateUserDataMutation.Data> createUserDataMutationCallback = new GraphQLCall.Callback<CreateUserDataMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateUserDataMutation.Data> response) {
            Log.i("Results", "Created blank user data");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", "Error creating user data: " + e.toString());
        }
    };

    private void updateUserDataMutation(String privateData, String preferences, String dietData) {
        UpdateUserDataInput updateUserDataInput = UpdateUserDataInput.builder()
                .id(settingObjectId)
                .privateData(privateData)
                .preferences(preferences)
                .dietData(dietData)
                .build();

        mAWSAppSyncClient.mutate(UpdateUserDataMutation.builder().input(updateUserDataInput).build())
                .enqueue(updateUserDataMutationCallback);
    }

    private GraphQLCall.Callback<UpdateUserDataMutation.Data> updateUserDataMutationCallback = new GraphQLCall.Callback<UpdateUserDataMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<UpdateUserDataMutation.Data> response) {
            assert response.data() != null; //TODO check
            Log.i("Results", "Updated user data: " + response.data().updateUserData().owner());
            Log.i("Results", "privateData: " + response.data().updateUserData().privateData());
            Log.i("Results", "preferences: " + response.data().updateUserData().preferences());
            Log.i("Results", "dietData: " + response.data().updateUserData().dietData());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }
    };

    private void userDataListQuery() {
        mAWSAppSyncClient.query(
                ListUserDatasQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(getUserDataListCallback);
    }

    private GraphQLCall.Callback<ListUserDatasQuery.Data> getUserDataListCallback = new GraphQLCall.Callback<ListUserDatasQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUserDatasQuery.Data> response) {
            assert response.data() != null; //TODO check
            userDataList = response.data().listUserDatas().items();
            settingObjectId = userDataList.get(0).id();
            userDataQuery(settingObjectId);
            Log.i("Results", "Received userData list: " + response.data().listUserDatas().toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", "Error when receiving userData list: " + e.toString());
        }
    };

    private void userDataQuery(String id) {
        mAWSAppSyncClient.query(GetUserDataQuery.builder()
                .id(id)
                .build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(getUserDataCallback);
    }

    private GraphQLCall.Callback<GetUserDataQuery.Data> getUserDataCallback = new GraphQLCall.Callback<GetUserDataQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetUserDataQuery.Data> response) {
            assert response.data() != null; //TODO check
            privateData = response.data().getUserData().privateData();
            preferences = response.data().getUserData().preferences();
            dietData = response.data().getUserData().dietData();
            refreshText();
            Log.i("Results", "Received single user data: " + response.data().getUserData().toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", "Error when getting single userData: " + e.toString());
        }
    };


}
