package dev.pl.clouddietapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.adapters.IntInputFilterMinMax;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.logic.Logic;
import dev.pl.clouddietapp.models.Gender;
import dev.pl.clouddietapp.models.UserData;

import static dev.pl.clouddietapp.logic.Logic.calculateBMR;

public class EditDataActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "EditDataActivity";

    TextView editDataBMRLabel;
    Button editDataSaveBtn, editDataLocationBtn;
    Spinner editDataSpinner;
    RadioButton editDataRadioFemale, editDataRadioMale;
    TextInputEditText editDataHeightTxt, editDataWeightTxt, editDataAgeTxt;

    Map<String, String> userAttributes;

    String activityLevel;
    LatLng location = null;
    //get access to location permission
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_edit_data, null, false);
        drawer.addView(contentView, 0);

        editDataBMRLabel = findViewById(R.id.editDataBMRLabel);
        editDataSaveBtn = findViewById(R.id.editDataSaveBtn);
        editDataLocationBtn = findViewById(R.id.editDataLocationBtn);
        editDataSpinner = findViewById(R.id.editDataSpinner);
        editDataRadioFemale = findViewById(R.id.editDataRadioFemale);
        editDataRadioMale = findViewById(R.id.editDataRadioMale);
        editDataHeightTxt = findViewById(R.id.editDataHeightTxt);
        editDataWeightTxt = findViewById(R.id.editDataWeightTxt);
        editDataAgeTxt = findViewById(R.id.editDataAgeTxt);

        editDataHeightTxt.setFilters(new InputFilter[]{new IntInputFilterMinMax("0", "250")});
        editDataWeightTxt.setFilters(new InputFilter[]{new IntInputFilterMinMax("0", "300")});
        editDataAgeTxt.setFilters(new InputFilter[]{new IntInputFilterMinMax("0", "130")});

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinnerActivity, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_multiline);
        editDataSpinner.setAdapter(adapter);
        editDataSpinner.setOnItemSelectedListener(this);

        Callback<Map<String, String>> userAttributesCallback = new Callback<Map<String, String>>() {
            @Override
            public void onResult(Map<String, String> result) {
                Log.d(TAG, "userAttributesCallback onResult: " + result.toString());

                userAttributes = result;
                String locationString = userAttributes.get("custom:location");
                assert locationString != null;
                locationString = locationString.substring(10);
                locationString = locationString.substring(0, locationString.length()-1);
                String[] locSplit = locationString.split(",");
                location = new LatLng(Double.valueOf(locSplit[0]), Double.valueOf(locSplit[1]));

                runOnUiThread(() -> {
                    editDataSpinner.setSelection(getIndex(editDataSpinner, userAttributes.get("custom:physicalActivity")));

//                    Log.d(TAG, "custom:gender: " + userAttributes.get("custom:gender"));

                    if (userAttributes.get("custom:gender").equals("1")) { //male
                        editDataRadioFemale.setChecked(false);
                        editDataRadioMale.setChecked(true);
                    } else {
                        editDataRadioFemale.setChecked(true);
                        editDataRadioMale.setChecked(false);
                    }

                    editDataHeightTxt.setText(userAttributes.get("custom:height"));
                    editDataWeightTxt.setText(userAttributes.get("custom:weight"));
                    editDataAgeTxt.setText(userAttributes.get("custom:age"));

                    parseUserData();

                    updateBMR();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Cannot get the data from the DB", Toast.LENGTH_SHORT).show();
                });
                Log.e(TAG, "onError: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        };
        AWSMobileClient.getInstance().getUserAttributes(userAttributesCallback);
    }

    private void parseUserData() {
        String[] activityArr = getResources().getStringArray(R.array.spinnerActivity);
        int index = Arrays.asList(activityArr).indexOf(userAttributes.get("custom:physicalActivity"));

        DataStore.setUserData(new UserData(AWSMobileClient.getInstance().getUsername(),
                userAttributes.get("custom:name"),
                Integer.valueOf(userAttributes.get("custom:age")),
                Integer.valueOf(userAttributes.get("custom:height")),
                Double.valueOf(userAttributes.get("custom:weight")),
                index,
                userAttributes.get("custom:gender").equals("1") ? Gender.MALE : Gender.FEMALE,
                userAttributes.get("custom:location"),
                DataStore.getUserData().getPreferences(),
                DataStore.getUserData().getRecommendedRecipes()
        ));

        Logic.appSyncDb.getUserData(null, null);
    }

    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }

    private void updateBMR() {
        DecimalFormat df2 = new DecimalFormat("#");
        editDataBMRLabel.setText(df2.format(calculateBMR()));
    }

    public void maleRadioHandler(View view) {
        if (editDataRadioFemale.isChecked()) {
            editDataRadioFemale.setChecked(false);
            editDataRadioMale.setChecked(true);
        }
    }

    public void femaleRadioHandler(View view) {
        if (editDataRadioMale.isChecked()) {
            editDataRadioMale.setChecked(false);
            editDataRadioFemale.setChecked(true);
        }
    }

    private boolean verifyData() {
        String age = editDataAgeTxt.getText().toString();
        String height = editDataHeightTxt.getText().toString();
        String weight = editDataWeightTxt.getText().toString();
        AtomicBoolean status = new AtomicBoolean(false);

        runOnUiThread(() -> {
            if (location == null)
                Toast.makeText(getApplicationContext(), "Choose your location", Toast.LENGTH_SHORT).show();
            else if (age.isEmpty())
                Toast.makeText(getApplicationContext(), "Empty age", Toast.LENGTH_SHORT).show();
            else if (Integer.valueOf(age) < 9)
                Toast.makeText(getApplicationContext(), "Age cannot be lower than 9", Toast.LENGTH_SHORT).show();
            else if (height.isEmpty())
                Toast.makeText(getApplicationContext(), "Empty height", Toast.LENGTH_SHORT).show();
            else if (weight.isEmpty())
                Toast.makeText(getApplicationContext(), "Empty weight", Toast.LENGTH_SHORT).show();
            else {
                status.set(true);
            }
        });

        return status.get();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        activityLevel = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void editDataLocationBtn(View view) {
        Intent i = new Intent(this, LocationPermissionActivity.class);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                location = data.getParcelableExtra("location");
                Log.d(TAG, "gotLocation: " + location);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Location not chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void saveBtnHandler(View view) {
        if (verifyData()) {
            Callback<List<UserCodeDeliveryDetails>> updateUserAttributesCallback = new Callback<List<UserCodeDeliveryDetails>>() {
                @Override
                public void onResult(List<UserCodeDeliveryDetails> result) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Successfully saved the data", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Cannot save the data", Toast.LENGTH_SHORT).show();
                    });
                    Log.e(TAG, "onError: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            };

            String age = editDataAgeTxt.getText().toString();
            String height = editDataHeightTxt.getText().toString();
            String weight = editDataWeightTxt.getText().toString();
            String gender = editDataRadioMale.isChecked() ? "1" : "0";
            String activity = activityLevel;

            Map<String, String> newAtr = new HashMap<>();

            newAtr.put("custom:age", age);
            newAtr.put("custom:height", height);
            newAtr.put("custom:weight", weight);
            newAtr.put("custom:gender", gender);
            newAtr.put("custom:physicalActivity", activity);
            newAtr.put("custom:location", location == null ? "" : location.toString());


            userAttributes.put("custom:age", age);
            userAttributes.put("custom:height", height);
            userAttributes.put("custom:weight", weight);
            userAttributes.put("custom:gender", gender);
            userAttributes.put("custom:physicalActivity", activity);
            userAttributes.put("custom:location", location == null ? "" : location.toString());

            parseUserData();
            updateBMR();

            AWSMobileClient.getInstance().updateUserAttributes(newAtr, updateUserAttributesCallback);
        }
    }
}
