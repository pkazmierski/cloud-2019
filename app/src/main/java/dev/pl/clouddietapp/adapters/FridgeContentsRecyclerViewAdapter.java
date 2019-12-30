package dev.pl.clouddietapp.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.models.Food;
import dev.pl.clouddietapp.models.FoodDefinition;

public class FridgeContentsRecyclerViewAdapter extends RecyclerView.Adapter<FridgeContentsRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "FridgeRVAdapter";

    private Context ctx;
    private int counter;
    private ArrayList<FoodDefinition> foodDefinitions;

    public FridgeContentsRecyclerViewAdapter(Context ctx, ArrayList<FoodDefinition> foodDefinitions) {
        this.ctx = ctx;
        this.foodDefinitions = foodDefinitions;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @NonNull
    @Override
    //responsible for inflating the view
    //can be always the same (beside the layout name)
    public FridgeContentsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridge_content_item, parent, false);
        FridgeContentsRecyclerViewAdapter.ViewHolder holder = new FridgeContentsRecyclerViewAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    //change what layout look like
    public void onBindViewHolder(@NonNull FridgeContentsRecyclerViewAdapter.ViewHolder holder, final int position) {
        holder.foodAmountTxt.setTag(position);
        FoodDefinition foodDefinition = foodDefinitions.get(position);
        //todo food może być null, jezeli user został dodany do bazy danych, ale user go jeszcze nie ma
        if (DataStore.getUserData().getFoodById(foodDefinition.getId()) == null) {
            DataStore.getUserData().getFridgeContents().add(new Food(foodDefinition.getId(), 0.0));
        }
        final Food food = DataStore.getUserData().getFoodById(foodDefinition.getId());

        holder.foodNameTxt.setText(foodDefinition.getName());
        holder.foodAmountTxt.setText(String.valueOf(food.getAmount()));
        holder.foodUnitTxt.setText(foodDefinition.getUnit());

//        holder.foodAmountTxt.addTextChangedListener(new TextWatcher() {
//            boolean isOnTextChanged = false;
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (s.toString().equals(""))
//                    food.setAmount(0);
//                else {
//                    Log.d(TAG, "afterTextChanged: food: " + counter + "\n" + food.toString() + "\nfoodDef: " + foodDefinition + "\nnewText: " + s.toString() + "\narray: " + foodDefinitions.toString());
//                    food.setAmount(Double.parseDouble(s.toString()));
//                    counter++;
//                }
//            }
//        });
        //todo listener na amount: niech ten amount sie od razu zapisuje
    }

    @Override
    public int getItemCount() {
        return foodDefinitions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //holds individual entries in memory
        TextView foodNameTxt;
        EditText foodAmountTxt;
        CardView foodCardTxt;
        TextView foodUnitTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameTxt = itemView.findViewById(R.id.foodNameTxt);
            foodAmountTxt = itemView.findViewById(R.id.foodAmountTxt);
            foodCardTxt = itemView.findViewById(R.id.foodCard);
            foodUnitTxt = itemView.findViewById(R.id.foodUnitTxt);
            foodAmountTxt.addTextChangedListener(new TextWatcher() {
                boolean isOnTextChanged = false;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d(TAG, "afterTextChanged: tssffdsfsfdsfds");
                    if (s.toString().equals("")) {
                        if (foodAmountTxt.getTag() != null) {
                            String id = foodDefinitions.get((int) foodAmountTxt.getTag()).getId();
                            DataStore.getUserData().getFoodById(id).setAmount(0.0);
                        }
                    } else {
                        if (foodAmountTxt.getTag() != null) {
                            String id = foodDefinitions.get((int) foodAmountTxt.getTag()).getId();
                            Food food = DataStore.getUserData().getFoodById(id);
                            Log.d(TAG, "afterTextChanged: food: " + counter + "\n" + food.toString() + "\nfoodDef: " + foodDefinitions.get((int) foodAmountTxt.getTag()) + "\nnewText: " + s.toString() + "\narray: " + foodDefinitions.toString());
                            food.setAmount(Double.valueOf(s.toString()));
                        }
                    }
                }
            });
        }
    }

    public void filterList(ArrayList<FoodDefinition> filteredFoods) {
        foodDefinitions = filteredFoods;
        notifyDataSetChanged();
    }
}

