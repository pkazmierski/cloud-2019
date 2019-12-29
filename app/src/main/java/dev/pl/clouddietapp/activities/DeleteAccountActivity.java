package dev.pl.clouddietapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;

import dev.pl.clouddietapp.R;

public class DeleteAccountActivity extends AppCompatActivity {

    Button deleteButton;
    CheckBox checkDelete;
    ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.GONE);

        bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.GONE);

        checkDelete = findViewById(R.id.deleteCheck);

    }

    public void checkDelete(View v) {
        checkDelete = (CheckBox)v;
        if(checkDelete.isChecked()){
            deleteButton.setVisibility(View.VISIBLE);
        }
    }

    public void deleteAccount(View v) {
        bar.setVisibility(View.VISIBLE);
        AWSConfiguration awsConfiguration = AWSMobileClient.getInstance().getConfiguration();
        CognitoUserPool userpool = new CognitoUserPool(this, awsConfiguration); // CognitoUserPool object
        GenericHandler logoutHandler = new GenericHandler() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(Exception exception) {
                Log.e("GLOBAL_LOGOUT", exception.getMessage());
            }
        };
        GenericHandler deleteHandler = new GenericHandler() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),"Your data was deleted", Toast.LENGTH_SHORT).show();
                userpool.getCurrentUser().globalSignOutInBackground(logoutHandler);
            }

            @Override
            public void onFailure(Exception exception) {
                Log.e("DELETE_USER", exception.getMessage());
            }
        };
        userpool.getCurrentUser().deleteUserInBackground(deleteHandler);

        AWSMobileClient.getInstance().signOut();
        finishAffinity();
        startActivity(new Intent(this, AuthenticationActivity.class));
    }
}
