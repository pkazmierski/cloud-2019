package dev.pl.clouddietapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;

import dev.pl.clouddietapp.R;

public class AuthenticationActivity extends AppCompatActivity {

    private final String TAG = AuthenticationActivity.class.getSimpleName();
    Button buttonLogin;
    EditText usrname, passwrd;
    TextView text, registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        usrname = (EditText) findViewById(R.id.signInUsername);
        passwrd = (EditText) findViewById(R.id.signInPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        registerLink = (TextView) findViewById(R.id.registerButton);

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, userStateDetails.getUserState().toString());
                if (userStateDetails.getUserState() == UserState.SIGNED_IN) {
                    finish();
                    Intent i = new Intent(AuthenticationActivity.this, DishesActivity.class);
                    startActivity(i);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

    }

    public void loginUser(View view) {

        final String username = usrname.getText().toString();
        final String password = passwrd.getText().toString();

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
                        Intent i = new Intent(AuthenticationActivity.this, DishesActivity.class);
                        startActivity(i);
                        break;
                    case SIGNED_OUT:
                        signInUser(username,password);
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                        signInUser(username,password);
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

    }


    private void signInUser(String username, String password) {

        AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
            @Override
            public void onResult(final SignInResult signInResult) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Sign-in callback state: " + signInResult.getSignInState());
                    switch (signInResult.getSignInState()) {
                        case DONE:
                            Toast.makeText(getApplicationContext(),"Sign-in done.", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(AuthenticationActivity.this, DishesActivity.class);
                            startActivity(i);
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

    }

    public void registerLink(View view) {
        Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(i);
    }

    public void forgotPassword(View view) {
        Intent i = new Intent(getApplicationContext(), ResetPasswordActivity.class);
        startActivity(i);
    }
}