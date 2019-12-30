package dev.pl.clouddietapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.data.AppSyncDb;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.logic.Logic;
import dev.pl.clouddietapp.models.UserData;
import dev.pl.clouddietapp.models.UserPreferences;

public class PreferenceActivity extends BaseActivity {

    EditText maxDistanceSupermarketInputTxt;
    Switch onlyVegetarianInputTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_preference);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_preference, null, false);
        drawer.addView(contentView, 0);

        maxDistanceSupermarketInputTxt = findViewById(R.id.maxDistanceSupermarketInputTxt);
        onlyVegetarianInputTxt = findViewById(R.id.onlyVegetarianInputTxt);

        maxDistanceSupermarketInputTxt.setText(DataStore.getUserData().getPreferences().getMaxSupermarketDistance());
        onlyVegetarianInputTxt.setChecked(DataStore.getUserData().getPreferences().isVegetarian());
    }

    public void saveSettingsToDb(View view) {
        Runnable afterPreferencesSetSuccess = () -> runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), "Preferences saved", Toast.LENGTH_SHORT).show());

        Runnable afterPreferencesSetFailed = () -> runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), "Failed to save the preferences", Toast.LENGTH_LONG).show());

        UserData newUserData = DataStore.getUserData();
        UserPreferences newUserPreferences = new UserPreferences(
                Integer.parseInt(maxDistanceSupermarketInputTxt.getText().toString()),
                onlyVegetarianInputTxt.isChecked());
        newUserData.setPreferences(newUserPreferences);

        Logic.appSyncDb.setUserData(afterPreferencesSetSuccess, afterPreferencesSetFailed, newUserData);
    }
}
