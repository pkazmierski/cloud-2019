package dev.pl.clouddietapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.adapters.FridgeContentsRecyclerViewAdapter;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.logic.Logic;
import dev.pl.clouddietapp.models.FoodDefinition;

public class FridgeContentsActivity extends BaseActivity {
    private static final String TAG = "FridgeContentsActivity";

    RecyclerView recyclerView;
    FridgeContentsRecyclerViewAdapter adapter;
    EditText filterFridgeContents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_fridge_contents);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_fridge_contents, null, false);
        drawer.addView(contentView, 0);

        filterFridgeContents = findViewById(R.id.filterFridgeContents);
        filterFridgeContents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        initRecyclerView();
//        Logic.appSyncDb.getFoodDefinitions(getAfterGetFoodDefinitionSuccess, afterGetFoodDefinitionFailure);
        Logic.appSyncDb.getUserData(getAfterGetUserDataSuccess, afterGetUserDataFailure);
    }

    private void filter(String text) {
        ArrayList<FoodDefinition> filteredFoodDefinitions = new ArrayList<>();

        for (FoodDefinition foodDefinition : DataStore.getFoodDefinitions()) {
            if(foodDefinition.getName().toLowerCase().contains(text.toLowerCase()))
                filteredFoodDefinitions.add(foodDefinition);
        }

        adapter.filterList(filteredFoodDefinitions);
    }

    private Runnable getAfterGetFoodDefinitionSuccess = () -> runOnUiThread(() ->
            adapter.notifyDataSetChanged());

    private Runnable getAfterGetUserDataSuccess = getAfterGetFoodDefinitionSuccess;

    private Runnable afterGetFoodDefinitionFailure = () -> runOnUiThread(() ->
            Toast.makeText(getApplicationContext(), "Failed to get food definitions", Toast.LENGTH_LONG).show());

    private Runnable afterGetUserDataFailure = () -> runOnUiThread(() ->
            Toast.makeText(getApplicationContext(), "Failed to get user data", Toast.LENGTH_LONG).show());

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.fridgeContentsRecyclerView);
        adapter = new FridgeContentsRecyclerViewAdapter(this, DataStore.getFoodDefinitions());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
