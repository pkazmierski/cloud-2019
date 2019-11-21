package dev.pl.clouddietapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.adapters.AddFridgeItemRecyclerViewAdapter;
import dev.pl.clouddietapp.data.AppSyncDb;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.logic.Logic;
import dev.pl.clouddietapp.models.Food;
import dev.pl.clouddietapp.models.FoodDefinition;
import dev.pl.clouddietapp.models.UserData;

public class AddFridgeItemActivity extends BaseActivity {

    private static final String TAG = "AddFridgeItemActivity";
    RecyclerView recyclerView;
    AddFridgeItemRecyclerViewAdapter adapter;
    EditText filterFoodDefinitionInputTxt;
    Button addFoodDefinitionsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_fridge_item);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_add_fridge_item, null, false);
        drawer.addView(contentView, 0);

        Logic.appSyncDb.getFoodDefinitions(afterFoodDefinitionsSuccess, afterFoodDefinitionsFailure);

        initRecyclerView();
    }

//    public void addItemsToFridge(View view) {
//        HashMap<FoodDefinition, Double> newFoodsHashMap = adapter.getNewFoods();
//
//        UserData newUserData = new UserData(DataStore.getUserData());
//        ArrayList<Food> newFridgeContents = newUserData.getFridgeContents();
//
//        for (FoodDefinition foodDefinition : newFoodsHashMap.keySet()) {
//            Food foodChange = new Food(foodDefinition.getId(), newFoodsHashMap.get(foodDefinition));
//
//            if(newFridgeContents.contains(foodChange)) { //food exists - change quantiti
//                Food currentFood = newFridgeContents.get(newFridgeContents.indexOf(foodChange));
//
//            }
////            newFridgeContents.add(new Food(foodDefinition.getId(), ));
//        }

    private Runnable afterFoodDefinitionsSuccess = () -> runOnUiThread(() ->
            adapter.notifyDataSetChanged());

    private Runnable afterFoodDefinitionsFailure = () -> runOnUiThread(() ->
            Toast.makeText(getApplicationContext(), "Failed to download food definitions", Toast.LENGTH_LONG).show());

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.addFridgeItemRecyclerView);
        adapter = new AddFridgeItemRecyclerViewAdapter(this, DataStore.getFoodDefinitions());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
