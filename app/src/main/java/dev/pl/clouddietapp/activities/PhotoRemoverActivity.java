package dev.pl.clouddietapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.logic.Logic;
import dev.pl.clouddietapp.models.DatabasePhoto;

public class PhotoRemoverActivity extends BaseActivity {
    private static final String TAG = "PhotoRemoverActivity";

    String localUrl;
    LinearLayout photoRemoverLinearLayout;
    Context ctx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_photo_remover, null, false);
        drawer.addView(contentView, 0);

        ctx = this;
        photoRemoverLinearLayout = findViewById(R.id.photoRemoverLinearLayout);
        final List<DatabasePhoto> databasePhotoList = new ArrayList<>();
        Runnable onUserPhotoDownloadSuccess = () -> deleteUserPhotos(databasePhotoList);
        Runnable onUserPhotoDownloadFailure = () -> runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Cannot download user photos", Toast.LENGTH_SHORT).show());

        Logic.appSyncDb.getPhotosForLoggedInUser(onUserPhotoDownloadSuccess, onUserPhotoDownloadFailure, databasePhotoList);
    }

    private void deleteUserPhotos(final List<DatabasePhoto> databasePhotoList) {
        for (DatabasePhoto databasePhoto : databasePhotoList) {
            final String localPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + databasePhoto;

            TransferObserver downloadObserver =
                    Logic.transferUtility.download(
                            databasePhoto.getS3PhotoId(),
                            new File(localPath));

            // Attach a listener to the observer to get state update and progress notifications
            downloadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        // Handle a completed upload.
                        localUrl = localPath;
                        ImageView imageView = new ImageView(ctx);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.bottomMargin = 10;
                        lp.topMargin = 10;

                        imageView.setLayoutParams(lp);
                        imageView.setAdjustViewBounds(true);
                        imageView.setContentDescription(databasePhoto.getS3PhotoId());
                        imageView.setImageBitmap(Logic.decodeSampledBitmapFromFile(localUrl, 400, 400));

                        //todo button i handler
                        Button button = new Button(ctx);
                        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp2.bottomMargin = 30;
                        lp2.topMargin = 5;

                        button.setLayoutParams(lp2);
                        button.setText("Delete");
                        button.setAllCaps(true);
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        button.setTextColor(getResources().getColor(R.color.quantum_white_100));

                        button.setOnClickListener(v -> {
                            Runnable onDynamoDeletion = () -> runOnUiThread(() -> {
                                imageView.setVisibility(View.GONE);
                                button.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Photo deleted", Toast.LENGTH_SHORT).show();
                            });
                            Runnable onS3Deletion = () -> deletePhotoFromDynamoDB(databasePhoto.getId(), onDynamoDeletion);
                            deletePhotoFromS3(databasePhoto.getS3PhotoId(), onS3Deletion);
                        });

                        //button and button handler
                        photoRemoverLinearLayout.addView(imageView);
                        photoRemoverLinearLayout.addView(button);
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
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private void deletePhotoFromDynamoDB(String dynamoPhotoId, Runnable onDeletionSuccess) {
        Runnable onDeletionFailure = () -> runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Cannot delete the photo (DB)", Toast.LENGTH_SHORT).show());
        Logic.appSyncDb.deletePhotoFromDynamo(onDeletionSuccess, onDeletionFailure, dynamoPhotoId);
    }
}
