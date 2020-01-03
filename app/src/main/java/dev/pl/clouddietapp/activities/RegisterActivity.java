package dev.pl.clouddietapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.logic.Logic;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    EditText name1, email1, username1, password1, verificationCode, age1, height1, weight1;
    Button buttonRegister, buttonLocation, buttonVerify;
    CheckBox checkBoxPassword;
    Spinner spinner;
    TextView name2, email2, username2, password2, verify, age, height2, weight2, activity;
    RadioGroup radioGroup;
    RadioButton radioButton;
    ScrollView scroll;
    String active;
    String gender = "1";

    LatLng location = null;

    private static final String TAG = "RegisterActivity";

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^.{10,}$");  //at least 10 characters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name1 = (EditText) findViewById(R.id.signUpName);
        email1 = (EditText) findViewById(R.id.signUpEmail);
        username1 = (EditText) findViewById(R.id.signUpUsername);
        password1 = (EditText) findViewById(R.id.signUpPassword);
        age1 = (EditText) findViewById(R.id.signUpAge);
        height1 = (EditText) findViewById(R.id.signUpHeight);
        weight1 = (EditText) findViewById(R.id.signUpWeight);
        verificationCode = (EditText) findViewById(R.id.verification_code);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        buttonVerify = (Button) findViewById(R.id.verifyButton);
        buttonLocation = (Button) findViewById(R.id.buttonLocation);
        checkBoxPassword = (CheckBox) findViewById(R.id.checkBoxPassword);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        scroll = (ScrollView) findViewById(R.id.scroll);

        name2 = (TextView) findViewById(R.id.name);
        email2 = (TextView) findViewById(R.id.email);
        username2 = (TextView) findViewById(R.id.username);
        password2 = (TextView) findViewById(R.id.password);
        age = (TextView) findViewById(R.id.age);
        height2 = (TextView) findViewById(R.id.height);
        weight2 = (TextView) findViewById(R.id.weight);
        activity = (TextView) findViewById(R.id.physicalActivity);
        spinner = (Spinner) findViewById(R.id.signUpPhysicalActivity);
        verify = (TextView) findViewById(R.id.verify);

        verify.setVisibility(View.GONE);
        verificationCode.setVisibility(View.GONE);
        buttonVerify.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinnerActivity, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_multiline);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        checkBoxPassword.setOnCheckedChangeListener((compoundButton, value) -> {
            if (value) {
                password1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                password1.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        password1.setOnFocusChangeListener((v, hasFocus) -> {
            if (!PASSWORD_PATTERN.matcher(password1.getText()).matches() && password1.getText().toString().length() < 10) {
                Toast.makeText(getApplicationContext(), "Password must contain at least 10 characters.", Toast.LENGTH_SHORT).show();
                password1.setError("Password must contain at least 10 characters.");
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        active = parent.getItemAtPosition(position).toString();
//        Toast.makeText(parent.getContext(), active, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void getLocationBtn(View view) {
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

    public void checkRadioButton (View v) {
        int radioId = radioGroup.getCheckedRadioButtonId();

        radioButton = (RadioButton) findViewById(radioId);

        if(radioId == R.id.male) {
            gender = "1";
        } else if (radioId == R.id.female) {
            gender = "0";
        }
    }

    public void registerUser(View view) {
        final String username = username1.getText().toString();
        final String password = password1.getText().toString();
        final String email = email1.getText().toString();
        final String name = name1.getText().toString();
        final String age = age1.getText().toString();
        final String height = height1.getText().toString();
        final String weight = weight1.getText().toString();
        checkRadioButton(view);

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("custom:name", name);
        attributes.put("custom:age", age);
        attributes.put("custom:height", height);//custom:height
        attributes.put("custom:weight", weight);//custom:weight
        attributes.put("custom:physicalActivity", active);//custom:physicalActivity
        attributes.put("custom:gender", gender);//custom:gender (0,1)
        attributes.put("custom:location", location == null ? "" : location.toString());//custom:location

        if(location == null) {
            Toast.makeText(getApplicationContext(), "Choose your location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(getApplicationContext(), "Enter username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(age)) {
            Toast.makeText(getApplicationContext(), "Enter age", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(height)) {
            Toast.makeText(getApplicationContext(), "Enter height", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(weight)) {
            Toast.makeText(getApplicationContext(), "Enter weight", Toast.LENGTH_SHORT).show();
            return;
        }

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, userStateDetails.getUserState().toString());
                switch (userStateDetails.getUserState()){
                    case SIGNED_IN:
                        finish();
                        Intent i = new Intent(RegisterActivity.this, RecipesActivity.class);
                        startActivity(i);
                        break;
                    case SIGNED_OUT:
                        signUpUser(username,password,attributes);
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                        signUpUser(username,password,attributes);
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    private void signUpUser(String username, String password, Map<String, String> attributes) {

        AWSMobileClient.getInstance().signUp(username, password, attributes, null, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Sign-up callback state: " + signUpResult.getConfirmationState());
                    if (!signUpResult.getConfirmationState()) {
                        final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                        Toast.makeText(getApplicationContext(),"Confirm sign-up with: " + details.getDestination(), Toast.LENGTH_SHORT).show();
                        updateUI();
                    } else {
                        Toast.makeText(getApplicationContext(),"Sign-up done.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (e.getClass().getSimpleName().equals("UsernameExistsException")){
                            Toast.makeText(getApplicationContext(),"User already exists.", Toast.LENGTH_SHORT).show();
                        } else if (e.getClass().getSimpleName().equals("InvalidParameterException")){
                            Toast.makeText(getApplicationContext(),"Incorrect attributes values and/or password must contain at least 10 characters.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.e(TAG, "Sign-up error", e);
            }
        });
    }

    private void updateUI() {

        name1.setVisibility(View.GONE);
        email1.setVisibility(View.GONE);
        username1.setVisibility(View.GONE);
        password1.setVisibility(View.GONE);
        age1.setVisibility(View.GONE);
        height1.setVisibility(View.GONE);
        weight1.setVisibility(View.GONE);
        activity.setVisibility(View.GONE);
        radioButton.setVisibility(View.GONE);
        radioGroup.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
        checkBoxPassword.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.GONE);
        buttonLocation.setVisibility(View.GONE);
        scroll.setVisibility(View.GONE);

        name2.setVisibility(View.GONE);
        email2.setVisibility(View.GONE);
        username2.setVisibility(View.GONE);
        password2.setVisibility(View.GONE);
        age.setVisibility(View.GONE);
        height2.setVisibility(View.GONE);
        weight2.setVisibility(View.GONE);


        verify.setVisibility(View.VISIBLE);
        verificationCode.setVisibility(View.VISIBLE);
        buttonVerify.setVisibility(View.VISIBLE);
    }

    public void verifyUser(View view) {

        final String code = verificationCode.getText().toString();
        final String username = username1.getText().toString();

        AWSMobileClient.getInstance().confirmSignUp(username, code, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Sign-up callback state: " + signUpResult.getConfirmationState());
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            Toast.makeText(getApplicationContext(),"Confirm sign-up with: " + details.getDestination(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "sign-up: first step of registration for " + username);
                            final String password = password1.getText().toString();

                            AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
                                @Override
                                public void onResult(final SignInResult signInResult) {
                                    runOnUiThread(() -> {
                                        Log.d(TAG, "Sign-in callback state: " + signInResult.getSignInState());
                                        switch (signInResult.getSignInState()) {
                                            case DONE:
                                                Toast.makeText(getApplicationContext(),"Sign-in done.", Toast.LENGTH_SHORT).show();

                                                DataStore.getUserData().setUsername(username);
                                                Logic.initAppSync(getApplicationContext());

                                                Runnable createUserDataSuccess = () -> {
                                                    Log.d(TAG, "sign-up: created user data in DynamoDB");
                                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(),"Sign-up done.", Toast.LENGTH_SHORT).show());
                                                    Intent i = new Intent(RegisterActivity.this, RecipesActivity.class);
                                                    startActivity(i);
                                                };
                                                Runnable createUserDataFailure = () -> Log.d(TAG, "sign-up: failed to create user data in DynamoDB");

                                                Logic.appSyncDb.createUserData(createUserDataSuccess, createUserDataFailure);

                                                break;
                                            case PASSWORD_VERIFIER:
                                                Toast.makeText(getApplicationContext(),"Enter correct password.", Toast.LENGTH_SHORT).show();
                                                break;
                                            case NEW_PASSWORD_REQUIRED:
                                                Toast.makeText(getApplicationContext(),"Please confirm sign-in with new password.", Toast.LENGTH_SHORT).show();
                                                break;
                                            default:
                                                Toast.makeText(getApplicationContext(),"Unsupported sign-in confirmation: " + signInResult.getSignInState(), Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            if (e.getClass().getSimpleName().equals("UserNotFoundException")){
                                                Toast.makeText(getApplicationContext(),"User does not exist.", Toast.LENGTH_SHORT).show();
                                            } else if (e.getClass().getSimpleName().equals("NotAuthorizedException")){
                                                Toast.makeText(getApplicationContext(),"Incorrect username or password.", Toast.LENGTH_SHORT).show();
                                            } else if (e.getClass().getSimpleName().equals("UserNotConfirmedException")){
                                                Toast.makeText(getApplicationContext(),"User is not confirmed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    Log.e(TAG, "Sign-in error", e);
                                }
                            });

//                            finish();
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (e.getClass().getSimpleName().equals("CodeMismatchException")){
                            Toast.makeText(getApplicationContext(),"Invalid verification code provided, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.e(TAG, "Confirm sign-up error", e);
            }
        });

    }
}
