package dev.pl.clouddietapp.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.models.FoodDefinition;

public class AddFridgeItemRecyclerViewAdapter extends RecyclerView.Adapter<AddFridgeItemRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "AddFridgeItemRVAdapter";

    private Context ctx;
    private ArrayList<FoodDefinition> foodDefinitions;
    private HashMap<FoodDefinition, Double> amounts = new HashMap<>();

    public AddFridgeItemRecyclerViewAdapter(Context ctx, ArrayList<FoodDefinition> foodDefinitions) {
        this.ctx = ctx;
        this.foodDefinitions = foodDefinitions;
    }

    @NonNull
    @Override
    //responsible for inflating the view
    //can be always the same (beside the layout name)
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_fooddefinition, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    //change what layout look like
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        FoodDefinition currentDefinition = foodDefinitions.get(position);

        holder.foodDefinitionAmountNameTxt.setText(currentDefinition.getName());
        holder.unitLabelTxt.setText(currentDefinition.getUnit());
        holder.foodDefinitionAmountInputTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                amounts.put(currentDefinition, Double.valueOf(s.toString()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodDefinitions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //holds individual entries in memory
        CardView cardViewFoodFefinition;
        TextView foodDefinitionAmountNameTxt;
        TextView unitLabelTxt;
        EditText foodDefinitionAmountInputTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewFoodFefinition = itemView.findViewById(R.id.cardViewFoodFefinition);
            foodDefinitionAmountNameTxt = itemView.findViewById(R.id.foodDefinitionAmountNameTxt);
            unitLabelTxt = itemView.findViewById(R.id.unitLabelTxt);
            foodDefinitionAmountInputTxt = itemView.findViewById(R.id.foodDefinitionAmountInputTxt);
        }
    }

    public HashMap<FoodDefinition, Double> getNewFoods() {
        return amounts;
    }
}
