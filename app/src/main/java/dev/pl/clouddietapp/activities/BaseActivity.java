package dev.pl.clouddietapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.google.android.material.navigation.NavigationView;

import dev.pl.clouddietapp.R;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
            startActivity(new Intent(this, DeleteAccountActivity.class));
        } else if (id == R.id.nav_map) {
            startAnimatedActivity(new Intent(getApplicationContext(), PermissionActivity.class));
        } else if (id == R.id.nav_preferences) {
            startAnimatedActivity(new Intent(getApplicationContext(), PreferenceActivity.class));
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void startAnimatedActivity(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }
}
