package dev.pl.clouddietapp.logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private static final String TAG = "Logic";

    public static AWSAppSyncClient appSyncClient = null;
    public static AppSyncDb appSyncDb = new AppSyncDb();
    private static boolean init = false;
    public static TransferUtility transferUtility = null;

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

            transferUtility = TransferUtility.builder()
                    .context(ctx)
                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                    .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                    .build();
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

    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
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
