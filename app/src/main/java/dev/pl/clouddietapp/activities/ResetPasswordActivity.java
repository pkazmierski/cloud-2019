package dev.pl.clouddietapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import dev.pl.clouddietapp.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.ForgotPasswordResult;

import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText username, code, password;
    Button buttonReset, buttonConfirm;
    CheckBox checkBoxPassword;
    TextView enter_username, username1, code1, password1;

    private static final String TAG = "ResetPasswordActivity";

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^.{10,}$");  //at least 10 characters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        username = (EditText) findViewById(R.id.resetUsername);
        code = (EditText) findViewById(R.id.conf_code);
        password = (EditText) findViewById(R.id.resetPass);

        buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonConfirm = (Button) findViewById(R.id.confirmButton);

        checkBoxPassword = (CheckBox) findViewById(R.id.checkBoxPass);

        enter_username = (TextView) findViewById(R.id.reset);
        username1 = (TextView) findViewById(R.id.resetUser);
        code1 = (TextView) findViewById(R.id.code1);
        password1 = (TextView) findViewById(R.id.textPassword);

        code.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        buttonConfirm.setVisibility(View.GONE);
        checkBoxPassword.setVisibility(View.GONE);
        code1.setVisibility(View.GONE);
        password1.setVisibility(View.GONE);

        checkBoxPassword.setOnCheckedChangeListener((compoundButton, value) -> {
            if (value) {
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        password.setOnFocusChangeListener((v, hasFocus) -> {
            if (!PASSWORD_PATTERN.matcher(password.getText()).matches()) {
                password.setError("Password must contain at least 10 characters.");
            }
            if (password.getText().chars().count() < 10) {
                password.setError("Password must contain at least 10 characters.");
            }
        });

    }

    public void resetPassword(View v) {

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, userStateDetails.getUserState().toString());
                if (userStateDetails.getUserState() == UserState.SIGNED_IN) {
                    finish();
                    Intent i = new Intent(ResetPasswordActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    reset();
                }
            }

                    @Override
                    public void onError(Exception e) {
                        Log.e("INIT", "Initialization error.", e);
                    }
                }
        );


    }

    private void reset(){

        final String usern = username.getText().toString();
        AWSMobileClient.getInstance().forgotPassword(usern, new Callback<ForgotPasswordResult>() {
            @Override
            public void onResult(final ForgotPasswordResult result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "forgot password state: " + result.getState());
                        switch (result.getState()) {
                            case CONFIRMATION_CODE:
                                Toast.makeText(getApplicationContext(),"Confirmation code is sent to email to reset password", Toast.LENGTH_SHORT).show();
                                updateUI();
                                break;
                            default:
                                Log.e(TAG, "un-supported forgot password state");
                                break;
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "forgot password error", e);
            }
        });
    }

    private void updateUI() {

        username.setVisibility(View.GONE);
        buttonReset.setVisibility(View.GONE);
        enter_username.setVisibility(View.GONE);
        username1.setVisibility(View.GONE);

        code.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);
        buttonConfirm.setVisibility(View.VISIBLE);
        checkBoxPassword.setVisibility(View.VISIBLE);
        code1.setVisibility(View.VISIBLE);
        password1.setVisibility(View.VISIBLE);

    }

    public void confirmPassword(View v) {

        final String pass = password.getText().toString();
        final String cod = code.getText().toString();

        AWSMobileClient.getInstance().confirmForgotPassword(pass, cod, new Callback<ForgotPasswordResult>() {
            @Override
            public void onResult(final ForgotPasswordResult result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "forgot password state: " + result.getState());
                        switch (result.getState()) {
                            case DONE:
                                Toast.makeText(getApplicationContext(),"Password changed successfully", Toast.LENGTH_SHORT).show();
                                finish();
                                Intent i = new Intent(ResetPasswordActivity.this, AuthenticationActivity.class);
                                startActivity(i);
                                break;
                            default:
                                Log.e(TAG, "un-supported forgot password state");
                                break;
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "forgot password error", e);
            }
        });
    }

}
