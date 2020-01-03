package dev.pl.clouddietapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;

import java.util.ArrayList;
import java.util.List;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.logic.Logic;
import dev.pl.clouddietapp.models.DatabasePhoto;

public class DeleteAccountActivity extends BaseActivity {

    Button deleteButton;
    CheckBox checkDelete;
    ProgressBar bar;
    private static final String TAG = "DeleteAccountActivity";
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_delete_account, null, false);
        drawer.addView(contentView, 0);

        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.GONE);

        bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.GONE);

        checkDelete = findViewById(R.id.deleteCheck);

        ctx = this;

    }

    public void checkDelete(View v) {
        checkDelete = (CheckBox)v;
        if(checkDelete.isChecked()){
            deleteButton.setVisibility(View.VISIBLE);
        }
    }

    public void deleteAccount(View v) {
        bar.setVisibility(View.VISIBLE);

        //pobranie listy wszystkich zdjec nalezacych do usera; usuniecie z s3 i dynamo
        final List<DatabasePhoto> databasePhotoList = new ArrayList<>();

        Runnable dynamoAndS3Done = () -> {
            AWSConfiguration awsConfiguration = AWSMobileClient.getInstance().getConfiguration();
            CognitoUserPool userpool = new CognitoUserPool(ctx, awsConfiguration); // CognitoUserPool object
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
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(),"Your data was deleted", Toast.LENGTH_SHORT).show());
                    userpool.getCurrentUser().globalSignOutInBackground(logoutHandler);
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.e("DELETE_USER", exception.getMessage());
                }
            };
            userpool.getCurrentUser().deleteUserInBackground(deleteHandler);

            AWSMobileClient.getInstance().signOut();
            runOnUiThread(() -> {
                finishAffinity();
                startActivity(new Intent(ctx, AuthenticationActivity.class));
            });
        };
        Runnable userDataDeletionFail = () -> Toast.makeText(getApplicationContext(), "Failed to delete user data", Toast.LENGTH_SHORT).show();
        Runnable onPhotoDeletionDone = () -> Logic.appSyncDb.deleteUserData(dynamoAndS3Done, userDataDeletionFail);

        Runnable onUserPhotoDownloadSuccess = () -> deleteUserPhotos(databasePhotoList, onPhotoDeletionDone);
        Runnable onUserPhotoDownloadFailure = () -> runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Cannot download user photos", Toast.LENGTH_SHORT).show());
        Logic.appSyncDb.getPhotosForLoggedInUser(onUserPhotoDownloadSuccess, onUserPhotoDownloadFailure, databasePhotoList);
        //usuniecie userdata



    }

    private void deleteUserPhotos(List<DatabasePhoto> databasePhotos, Runnable onPhotoDeletionDone) {
        for (int i = 0; i < databasePhotos.size()-1; i++) {
            DatabasePhoto photo = databasePhotos.get(i);
            Runnable onDeletePhotoFromS3Success = () -> deletePhotoFromDynamoDB(photo.getId(), null);
            deletePhotoFromS3(photo.getS3PhotoId(), onDeletePhotoFromS3Success);
        }
        if(databasePhotos.size() > 0) {
            DatabasePhoto lastPhoto = databasePhotos.get(databasePhotos.size() - 1);
            Runnable onDeletePhotoFromS3Success = () -> deletePhotoFromDynamoDB(lastPhoto.getId(), onPhotoDeletionDone);
            deletePhotoFromS3(lastPhoto.getS3PhotoId(), onDeletePhotoFromS3Success);
        } else {
            onPhotoDeletionDone.run();
        }
    }

    private void deletePhotoFromS3(String s3PhotoId, Runnable onDeletionSuccess) {
        AmazonS3Client amazonS3Client = new AmazonS3Client(AWSMobileClient.getInstance());
        Log.d(TAG, "deletePhotoFromS3: deleting photo with key: " + s3PhotoId);
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest("clouddietapp1ada4a965c984f35bc2d90c14f2ba045-cldietenv", s3PhotoId);

        Thread thread = new Thread(() -> {
            try {
                amazonS3Client.deleteObject(deleteObjectRequest);
                onDeletionSuccess.run();
            } catch (Exception e) {
                Log.e(TAG, "deletePhotoFromS3: ", e);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Cannot delete the photo (S3)", Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
    }

    private void deletePhotoFromDynamoDB(String dynamoPhotoId, Runnable onDeletionSuccess) {
        Runnable onDeletionFailure = () ->  {
            Log.e(TAG, "deletePhotoFromDynamoDB: failed to delete the photo " + dynamoPhotoId);
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Cannot delete user photos from the DB", Toast.LENGTH_SHORT).show());
        };
        Logic.appSyncDb.deletePhotoFromDynamo(onDeletionSuccess, onDeletionFailure, dynamoPhotoId);
    }
}
