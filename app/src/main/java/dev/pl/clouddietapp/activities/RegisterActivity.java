package dev.pl.clouddietapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import dev.pl.clouddietapp.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText name1, email1, username1, password1, verificationCode;
    Button buttonRegister, buttonVerify;
    CheckBox checkBoxPassword;
    TextView name2, email2, username2, password2, verify;
    private static final String TAG = "RegisterActivity";

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" + ".{10,}" +  "$");  //at least 10 characters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name1 = (EditText) findViewById(R.id.signUpName);
        email1 = (EditText) findViewById(R.id.signUpEmail);
        username1 = (EditText) findViewById(R.id.signUpUsername);
        password1 = (EditText) findViewById(R.id.signUpPassword);
        verificationCode = (EditText) findViewById(R.id.verification_code);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        buttonVerify = (Button) findViewById(R.id.verifyButton);
        checkBoxPassword = (CheckBox) findViewById(R.id.checkBoxPassword);

        name2 = (TextView) findViewById(R.id.name);
        email2 = (TextView) findViewById(R.id.email);
        username2 = (TextView) findViewById(R.id.username);
        password2 = (TextView) findViewById(R.id.password);
        verify = (TextView) findViewById(R.id.verify);

        verify.setVisibility(View.GONE);
        verificationCode.setVisibility(View.GONE);
        buttonVerify.setVisibility(View.GONE);

        checkBoxPassword.setOnCheckedChangeListener((compoundButton, value) -> {
            if (value) {
                password1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                password1.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        password1.setOnFocusChangeListener((v, hasFocus) -> {

            if (!PASSWORD_PATTERN.matcher(password1.getText()).matches()) {
                password1.setError("Password must contain at least 10 characters.");
            }
        });
    }

    public void registerUser(View view) {
        final String username = username1.getText().toString();
        final String password = password1.getText().toString();
        final String email = email1.getText().toString();
        final String name = name1.getText().toString();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("custom:name", name);

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

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, userStateDetails.getUserState().toString());
                switch (userStateDetails.getUserState()){
                    case SIGNED_IN:
                        finish();
                        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
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
                Log.e(TAG, "Sign-up error", e);
            }
        });
    }

    private void updateUI() {

        name1.setVisibility(View.GONE);
        email1.setVisibility(View.GONE);
        username1.setVisibility(View.GONE);
        password1.setVisibility(View.GONE);
        checkBoxPassword.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.GONE);

        name2.setVisibility(View.GONE);
        email2.setVisibility(View.GONE);
        username2.setVisibility(View.GONE);
        password2.setVisibility(View.GONE);

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
                            Toast.makeText(getApplicationContext(),"Sign-up done.", Toast.LENGTH_SHORT).show();
                            finish();
                            Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Confirm sign-up error", e);
            }
        });

    }
}
