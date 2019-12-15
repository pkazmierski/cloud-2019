package dev.pl.clouddietapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.ForgotPasswordResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Toast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import dev.pl.clouddietapp.R;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
//    FloatingActionButton fab;
    NavigationView navigationView;
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("INIT", "onResult: " + userStateDetails.getUserState());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("INIT", "Initialization error.", e);
                    }
                }
        );

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            startAnimatedActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else if (id == R.id.nav_logout) {
            AWSMobileClient.getInstance().signOut();
            finishAffinity();
            startActivity(new Intent(this, AuthenticationActivity.class));
        } else if (id == R.id.nav_delete) {
            AlertDialog diaBox = AskOption();
            diaBox.show();
        }
//        else if (id == R.id.nav_activity2) {
//            startAnimatedActivity(new Intent(getApplicationContext(), SecondActivity.class));
//        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void startAnimatedActivity(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Do you want to delete your account?")

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteAccount();
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();

        return myQuittingDialogBox;
    }

    protected void deleteAccount(){

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("email", "empty@email.com");
        attributes.put("custom:name", " ");
        attributes.put("custom:age", "9");
        attributes.put("custom:height", "0");//custom:height
        attributes.put("custom:weight", "0");//custom:weight
        attributes.put("custom:physicalActivity", " ");//custom:physicalActivity
        attributes.put("custom:gender", "0");//custom:gender (0,1)
        //custom:location

        AWSMobileClient.getInstance().updateUserAttributes(attributes, new Callback<List<UserCodeDeliveryDetails>>() {
            @Override
            public void onResult(List<UserCodeDeliveryDetails> result) {
                Toast.makeText(getApplicationContext(),"Your data was deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                //Toast.makeText(getApplicationContext(),"Delete error", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
