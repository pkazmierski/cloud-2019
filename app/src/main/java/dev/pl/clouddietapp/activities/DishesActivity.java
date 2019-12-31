package dev.pl.clouddietapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.logic.Logic;

public class DishesActivity extends BaseActivity {

    View rootView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_dishes, null, false);
        drawer.addView(contentView, 0);

        Logic.initAppSync(this);
        DataStore.getUserData().setUsername(AWSMobileClient.getInstance().getUsername());

        Logic.appSyncDb.getUserAttributes(null, null, this);
        Logic.appSyncDb.getUserData(null, null);

        rootView = findViewById(R.id.dishes_layout);

        TextView breakfastTypeLabel = rootView.findViewById(R.id.dish_list_breakfast).findViewById(R.id.dishTypeLabel);
        breakfastTypeLabel.setText("Breakfast");

        TextView secondBreakfastTypeLabel = rootView.findViewById(R.id.dish_list_secondbreakfast).findViewById(R.id.dishTypeLabel);
        secondBreakfastTypeLabel.setText("Second breakfast");

        TextView dinnerTypeLabel = rootView.findViewById(R.id.dish_list_dinner).findViewById(R.id.dishTypeLabel);
        dinnerTypeLabel.setText("Dinner");

        TextView afterDinnerTypeLabel = rootView.findViewById(R.id.dish_list_afterdinner).findViewById(R.id.dishTypeLabel);
        afterDinnerTypeLabel.setText("After dinner");

        TextView supperTypeLabel = rootView.findViewById(R.id.dish_list_supper).findViewById(R.id.dishTypeLabel);
        supperTypeLabel.setText("Supper");

        //call recommendations
    }
}
