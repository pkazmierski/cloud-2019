package dev.pl.clouddietapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.data.DataStore;

public class FridgeContentsActivity extends BaseActivity {
    private static final String TAG = "FridgeContentsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_fridge_contents);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_fridge_contents, null, false);
        drawer.addView(contentView, 0);

        Log.d(TAG, "fridge contents: " + DataStore.getUserData().getFridgeContents().toString());
    }
}
