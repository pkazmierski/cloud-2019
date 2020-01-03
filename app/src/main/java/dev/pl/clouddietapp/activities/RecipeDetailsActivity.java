package dev.pl.clouddietapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amazonaws.amplify.generated.graphql.CreateRecipePhotoMutation;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.logic.Logic;
import dev.pl.clouddietapp.models.Recipe;
import type.CreateRecipePhotoInput;

import static dev.pl.clouddietapp.logic.Logic.appSyncClient;
import static dev.pl.clouddietapp.logic.Logic.calculateMD5;

public class RecipeDetailsActivity extends BaseActivity {
    private static final String TAG = "RecipeDetailsActivity";

    private String recipeId = null;
    String localUrl;

    // Photo selector application code.
    private static int RESULT_LOAD_IMAGE = 1;
    private String photoPath;

    TextView recipeDescriptionTextView;
    LinearLayout descAndPhotosLinearLayout;

    Recipe recipe = new Recipe();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_recipe_details, null, false);
        drawer.addView(contentView, 0);

        Button addPhotoBtn = findViewById(R.id.addPhotoBtn);
        recipeDescriptionTextView = findViewById(R.id.recipeDescriptionTextView);
        descAndPhotosLinearLayout = findViewById(R.id.descAndPhotosLinearLayout);

        ctx = this;

        Bundle b = getIntent().getExtras();
        recipeId = Objects.requireNonNull(b).getString("id");
        if (recipeId == null) {
            Log.e(TAG, "recipeId cannot be null ");
            addPhotoBtn.setEnabled(false);
        } else {
            Runnable getRecipeByIdSuccess = () -> recipeDescriptionTextView.setText(recipe.getContent());
            Runnable getRecipeByIdFailure = () -> Log.e(TAG, "cannot get the recipe from cache: " + recipeId);
            Logic.appSyncDb.getRecipeById(getRecipeByIdSuccess, getRecipeByIdFailure, recipeId, recipe, AppSyncResponseFetchers.CACHE_ONLY);

            List<String> photoIds = new ArrayList<>();
            Runnable getPhotosForRecipeSuccess = () -> downloadImagesAndAddToLayout(photoIds);
            Runnable getPhotosForRecipeFailure = () -> Log.e(TAG, "cannot get the recipe photo IDs from cache: " + recipeId);
            Logic.appSyncDb.getPhotosForRecipe(getPhotosForRecipeSuccess, getPhotosForRecipeFailure, recipeId, photoIds, AppSyncResponseFetchers.NETWORK_FIRST);
        }
    }

    public void choosePhoto(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            // String picturePath contains the path of selected Image
            photoPath = picturePath;

            uploadPhotoIfChosen();
        }
    }

    private void uploadPhotoIfChosen() {
        if (photoPath != null) {
            // For higher Android levels, we need to check permission at runtime
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                Log.d(TAG, "READ_EXTERNAL_STORAGE permission not granted! Requesting...");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
            // Upload a photo first. We will only call save on its successful callback.
            uploadWithTransferUtility(photoPath);
        }
    }

    private String getS3Key(String localPath) {
        //We have read and write ability under the public folder
        return "public/" + new File(localPath).getName();
    }

    public void uploadWithTransferUtility(String localPath) {
        File photoFile = new File(localPath);
        String key = getS3Key(calculateMD5(photoFile) + "_" + sdf.format(new Date()));

        Log.d(TAG, "Uploading file from " + localPath + " to " + key);

        TransferObserver uploadObserver =
                Logic.transferUtility.upload(
                        key,
                        photoFile);

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.d(TAG, "Upload to S3 completed.");
                    addPhotoToDynamoDB(recipeId, key);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;

                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Log.e(TAG, "Failed to upload photo. ", ex);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RecipeDetailsActivity.this, "Failed to upload photo", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void addPhotoToDynamoDB(String recipeId, String photoS3Key) {
        try {
            GraphQLCall.Callback<CreateRecipePhotoMutation.Data> createRecipePhotoCallback = new GraphQLCall.Callback<CreateRecipePhotoMutation.Data>() {
                @Override
                public void onResponse(@Nonnull Response<CreateRecipePhotoMutation.Data> response) {
                    if (response.hasErrors()) {
                        Log.e(TAG, "CreateRecipePhotoMutation: " + response.errors());
                        return;
                    }

                    if (response.data() == null) {
                        Log.d("CreateRecipePhotoMutation", "response.data() is null");
                    } else if (response.data().createRecipePhoto() == null) {
                        Log.d("CreateRecipePhotoMutation", "response.data().createRecipePhoto() is null");
                    } else {
                        Log.d("CreateRecipePhotoMutation", "Saving photo to DynamoDb completed: " + Objects.requireNonNull(response.data().createRecipePhoto()).toString());
                    }
                }

                @Override
                public void onFailure(@Nonnull ApolloException e) {
                    Log.e("UpdateRecipeMutation", e.toString());
                }
            };

            boolean ret = false;
            if (recipeId == null || recipeId.isEmpty()) {
                Log.e(TAG, "CreateRecipePhotoMutation: recipeId null or empty");
                ret = true;
            }
            if(photoS3Key == null || photoS3Key.isEmpty()) {
                Log.e(TAG, "CreateRecipePhotoMutation: photoS3Key null or empty");
                ret = true;
            }
            if(ret) return;

            CreateRecipePhotoInput createRecipePhotoInput = CreateRecipePhotoInput.builder()
                    .recipeId(recipeId)
                    .storagePhotoId(photoS3Key)
                    .build();

            appSyncClient.mutate(CreateRecipePhotoMutation.builder()
                    .input(createRecipePhotoInput)
                    .build())
                    .enqueue(createRecipePhotoCallback);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadImagesAndAddToLayout(final List<String> photos) {
        for (String photo : photos) {
            final String localPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + photo;

            TransferObserver downloadObserver =
                    Logic.transferUtility.download(
                            photo,
                            new File(localPath));

            // Attach a listener to the observer to get state update and progress notifications
            downloadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        // Handle a completed upload.
                        localUrl = localPath;
                        //todo dynamiczne dodawanie imageview
                        ImageView imageView = new ImageView(ctx);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.bottomMargin = 10;
                        lp.topMargin = 10;

                        imageView.setLayoutParams(lp);
                        imageView.setAdjustViewBounds(true);
                        imageView.setContentDescription(photo);
                        imageView.setImageBitmap(Logic.decodeSampledBitmapFromFile(localUrl, 400, 400));

                        descAndPhotosLinearLayout.addView(imageView);
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                    int percentDone = (int) percentDonef;

                    Log.d(TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
                }

                @Override
                public void onError(int id, Exception ex) {
                    // Handle errors
                    Log.e(TAG, "Unable to download the file.", ex);
                }
            });
        }


    }
}
