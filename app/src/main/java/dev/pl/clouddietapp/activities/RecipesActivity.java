package dev.pl.clouddietapp.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;

import java.util.ArrayList;
import java.util.List;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.data.DataStore;
import dev.pl.clouddietapp.logic.Logic;
import dev.pl.clouddietapp.models.Recipe;
import dev.pl.clouddietapp.models.RecipeType;

public class RecipesActivity extends BaseActivity {
    private static final String TAG = "RecipesActivity";

    View rootView;
    TextView breakfastNameLabel, secondBreakfastNameLabel, dinnerNameLabel, afterDinnerNameLabel, supperNameLabel;
    View dish_list_breakfast, dish_list_secondbreakfast, dish_list_dinner, dish_list_afterdinner, dish_list_supper;
    Button regenerateRecommendationsBtn;
    ProgressDialog dialog;

    @SuppressLint({"SetTextI18n", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_recipes, null, false);
        drawer.addView(contentView, 0);

        Logic.initAppSync(this);
        if(DataStore.getUserData().getUsername() == null || DataStore.getUserData().getUsername().isEmpty())
            DataStore.getUserData().setUsername(AWSMobileClient.getInstance().getUsername());

        Logic.appSyncDb.getUserAttributes(null, null, this);

        Logic.appSyncDb.getUserData(getUserDataSuccess, null);

        rootView = findViewById(R.id.dishes_layout);
        regenerateRecommendationsBtn = findViewById(R.id.regenerateRecommendationsBtn);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Generating recommendations...");
        dialog.setCancelable(true);

        //set labels
        {
            dish_list_breakfast = rootView.findViewById(R.id.dish_list_breakfast);
            TextView breakfastTypeLabel = dish_list_breakfast.findViewById(R.id.dishTypeLabel);
            breakfastTypeLabel.setText("Breakfast");

            dish_list_secondbreakfast = rootView.findViewById(R.id.dish_list_secondbreakfast);
            TextView secondBreakfastTypeLabel = dish_list_secondbreakfast.findViewById(R.id.dishTypeLabel);
            secondBreakfastTypeLabel.setText("Second breakfast");

            dish_list_dinner = rootView.findViewById(R.id.dish_list_dinner);
            TextView dinnerTypeLabel = dish_list_dinner.findViewById(R.id.dishTypeLabel);
            dinnerTypeLabel.setText("Dinner");

            dish_list_afterdinner = rootView.findViewById(R.id.dish_list_afterdinner);
            TextView afterDinnerTypeLabel = dish_list_afterdinner.findViewById(R.id.dishTypeLabel);
            afterDinnerTypeLabel.setText("After dinner");

            dish_list_supper = rootView.findViewById(R.id.dish_list_supper);
            TextView supperTypeLabel = dish_list_supper.findViewById(R.id.dishTypeLabel);
            supperTypeLabel.setText("Supper");
        }

        breakfastNameLabel = rootView.findViewById(R.id.dish_list_breakfast).findViewById(R.id.dishNameLabel);
        secondBreakfastNameLabel = rootView.findViewById(R.id.dish_list_secondbreakfast).findViewById(R.id.dishNameLabel);
        dinnerNameLabel = rootView.findViewById(R.id.dish_list_dinner).findViewById(R.id.dishNameLabel);
        afterDinnerNameLabel = rootView.findViewById(R.id.dish_list_afterdinner).findViewById(R.id.dishNameLabel);
        supperNameLabel = rootView.findViewById(R.id.dish_list_supper).findViewById(R.id.dishNameLabel);

        //test code
    }

    private Runnable getUserDataSuccess = () -> {
        if (DataStore.getUserData().getRecommendedRecipes() != null && DataStore.getUserData().getRecommendedRecipes().size() > 0) { //got recommendations to display
            //todo order may not be correct; in that case, we should just find a recipe with the appropriate type (make a function for it in UserData
            //todo or just sort the array manually
            runOnUiThread(() -> {
                List<Recipe> recommendations = DataStore.getUserData().getRecommendedRecipes();

                breakfastNameLabel.setText(recommendations.get(0).getName());
                secondBreakfastNameLabel.setText(recommendations.get(1).getName());
                dinnerNameLabel.setText(recommendations.get(2).getName());
                afterDinnerNameLabel.setText(recommendations.get(3).getName());
                supperNameLabel.setText(recommendations.get(4).getName());

                Intent recipeDetails = new Intent(this, RecipeDetailsActivity.class);
                dish_list_breakfast.setOnClickListener(v -> {
                    recipeDetails.putExtra("id", recommendations.get(0).getId());
                    startAnimatedActivity(recipeDetails);
                });

                dish_list_secondbreakfast.setOnClickListener(v -> {
                    recipeDetails.putExtra("id", recommendations.get(1).getId());
                    startAnimatedActivity(recipeDetails);
                });

                dish_list_dinner.setOnClickListener(v -> {
                    recipeDetails.putExtra("id", recommendations.get(2).getId());
                    startAnimatedActivity(recipeDetails);
                });

                dish_list_afterdinner.setOnClickListener(v -> {
                    recipeDetails.putExtra("id", recommendations.get(3).getId());
                    startAnimatedActivity(recipeDetails);
                });

                dish_list_supper.setOnClickListener(v -> {
                    recipeDetails.putExtra("id", recommendations.get(4).getId());
                    startAnimatedActivity(recipeDetails);
                });

                regenerateRecommendationsBtn.setEnabled(true);
            });
        } else {
            Runnable gotUserAttributes = () -> {
                generateRecommendationsAndSaveToProfile();
                Runnable updatedUserData = () -> Log.d(TAG, "updated user data");
                Logic.appSyncDb.updateUserData(updatedUserData, null, DataStore.getUserData());
                runOnUiThread(() -> regenerateRecommendationsBtn.setEnabled(true));
            };
            Logic.appSyncDb.getUserAttributes(gotUserAttributes, null, this);
        }
    };

    private void generateRecommendationsAndSaveToProfile() {
        //        Runnable getFilteredRecipesSuccess = () -> Log.d("regenerateRecommendationsBtn", "gotFilteredRecipes");
        Log.d(TAG, "generateRecommendationsAndSaveToProfile");

        runOnUiThread(() -> dialog.show());

        double bmr = Logic.calculateBMR();

        List<Recipe> breakfasts = new ArrayList<>();
        List<Recipe> secondBreakfasts = new ArrayList<>();
        List<Recipe> dinners = new ArrayList<>();
        List<Recipe> afterDinners = new ArrayList<>();
        List<Recipe> suppers = new ArrayList<>();
        Logic.appSyncDb.getFilteredRecipes(null, null, RecipeType.BREAKFAST, (int) (bmr * 0.24), breakfasts);
        Logic.appSyncDb.getFilteredRecipes(null, null, RecipeType.SECOND_BREAKFAST, (int) (bmr * 0.145), secondBreakfasts);
        Logic.appSyncDb.getFilteredRecipes(null, null, RecipeType.DINNER, (int) (bmr * 0.31), dinners);
        Logic.appSyncDb.getFilteredRecipes(null, null, RecipeType.AFTER_DINNER, (int) (bmr * 0.10), afterDinners);
        Logic.appSyncDb.getFilteredRecipes(null, null, RecipeType.SUPPER, (int) (bmr * 0.205), suppers);

        while (true) {
            if (breakfasts.size() > 0 && secondBreakfasts.size() > 0 && dinners.size() > 0 && afterDinners.size() > 0 && suppers.size() > 0)
                break;
        }

        List<Recipe> combined = new ArrayList<>();
        combined.addAll(breakfasts);
        combined.addAll(secondBreakfasts);
        combined.addAll(dinners);
        combined.addAll(afterDinners);
        combined.addAll(suppers);

        List<Recipe> recommended = Logic.recommendRecipes(combined);
        DataStore.getUserData().setRecommendedRecipes(recommended);
        getUserDataSuccess.run();

        runOnUiThread(() -> dialog.dismiss());
    }

    public void regenerateRecommendationsBtn(View view) {
        generateRecommendationsAndSaveToProfile();
    }


//    private void addTestDishesToUserProfile() {
//        Recipe rec1 = new Recipe("f053159d-5d6c-4837-9f07-4b2dd2a061f0", "Płatki jaglane z mango", "Płatki jaglane zalać gorącą lub wrzącą wodą na około 5 minut. Do płatków dodać jogurt, łyżeczkę kakao, nasiona słonecznika i cynamon, całość wymieszać. Jaglankę przełożyć do miseczki, dodać pokrojone kostkę mango.", null, RecipeType.BREAKFAST, 424);
//        Recipe rec2 =  new Recipe("dbc9ef52-146c-4cbb-88ae-21616cfbf5ff", "Koktajl awokado-kiwi-banan", "Owoce obrać, pokroić na drobniejsze kawałki i wrzucić do blendera. Dodać jogurt oraz miód i zblendować do otrzymania jednolitej konsystencji.", null, RecipeType.SECOND_BREAKFAST, 256);
//        Recipe rec3 = new Recipe("a642c5c6-4d38-41a0-beef-a637c8b642d3", "Kasza gryczana z wołowiną i grzybami + surówka z kapusty pekińskiej", "Wołowinę pokrojoną w paseczki lub kostkę zamarynować w ulubiony sposób, np. w naturalnych przyprawach (zioła prowansalskie, papryka słodka), przez noc. Kaszę ugotować zgodnie z instrukcją na opakowaniu. Suszone grzyby pokruszyć i wrzucić do szklanki z gorącą wodą, by zmiękły. Wołowinę podsmażyć na patelni natłuszczonej olejem rzepakowym. Dodać pokrojoną w kostkę cebulę i czosnek, podlać odrobiną wody, gdyby przywierały. Dokładnie wymieszać. Ugotowaną kaszę wymieszać z odsączonymi grzybami, zawartością patelni oraz tymiankiem. Kapustę poszatkować , ogórka pokroić w kostkę. Następnie przełożyć warzywa do miseczki i dodać olej rzepakowy. Przyprawić solą, pieprzem i koperkiem suszonym. Całość wymieszać.", null, RecipeType.DINNER, 548);
//        Recipe rec4 = new Recipe("88d4f561-312a-47fb-abfe-72fdd0932cef", "Sok buraczkowy", "Owoce i warzywa umyć i wycisnąć w wyciskarce.", null, RecipeType.AFTER_DINNER, 176);
//        Recipe rec5 = new Recipe("58e7f74f-4d66-4778-85f4-6ea23d8fb8cf", "Zupa krem brokułowo-szpinakowa z ryżem", "Ryż ugotować zgodnie z instrukcją na opakowaniu. Ziemniaka i marchew obrać, umyć i pokroić w kostkę. Dodać do nich różyczki brokułów i umyty szpinak. Całość zalać bulionem i gotować do miękkości. Następnie wszystko zmiksować i w razie potrzeby uzupełnić wodą lub bulionem. Zupę doprawić do smaku solą, pieprzem i czosnkiem, podawać z pokruszonym serem feta i ryżem i łyżeczką oleju lnianego (dodanego bezpośrednio na talerz).", null, RecipeType.SUPPER, 363);
//
//        List<Recipe> recs = new ArrayList<>();
//        recs.add(rec1);
//        recs.add(rec2);
//        recs.add(rec3);
//        recs.add(rec4);
//        recs.add(rec5);
//        DataStore.getUserData().setRecommendedRecipes(recs);
//
//        Logic.appSyncDb.updateUserData(null, null, DataStore.getUserData());
//    }

//    private void addTestRecipes() {
//        Recipe rec1 = new Recipe("", "Płatki jaglane z mango", "Płatki jaglane zalać gorącą lub wrzącą wodą na około 5 minut. Do płatków dodać jogurt, łyżeczkę kakao, nasiona słonecznika i cynamon, całość wymieszać. Jaglankę przełożyć do miseczki, dodać pokrojone kostkę mango.", null, RecipeType.BREAKFAST, 424);
//        Recipe rec2 =  new Recipe("", "Koktajl awokado-kiwi-banan", "Owoce obrać, pokroić na drobniejsze kawałki i wrzucić do blendera. Dodać jogurt oraz miód i zblendować do otrzymania jednolitej konsystencji.", null, RecipeType.SECOND_BREAKFAST, 256);
//        Recipe rec3 = new Recipe("", "Kasza gryczana z wołowiną i grzybami + surówka z kapusty pekińskiej", "Wołowinę pokrojoną w paseczki lub kostkę zamarynować w ulubiony sposób, np. w naturalnych przyprawach (zioła prowansalskie, papryka słodka), przez noc. Kaszę ugotować zgodnie z instrukcją na opakowaniu. Suszone grzyby pokruszyć i wrzucić do szklanki z gorącą wodą, by zmiękły. Wołowinę podsmażyć na patelni natłuszczonej olejem rzepakowym. Dodać pokrojoną w kostkę cebulę i czosnek, podlać odrobiną wody, gdyby przywierały. Dokładnie wymieszać. Ugotowaną kaszę wymieszać z odsączonymi grzybami, zawartością patelni oraz tymiankiem. Kapustę poszatkować , ogórka pokroić w kostkę. Następnie przełożyć warzywa do miseczki i dodać olej rzepakowy. Przyprawić solą, pieprzem i koperkiem suszonym. Całość wymieszać.", null, RecipeType.DINNER, 548);
//        Recipe rec4 = new Recipe("", "Sok buraczkowy", "Owoce i warzywa umyć i wycisnąć w wyciskarce.", null, RecipeType.AFTER_DINNER, 176);
//        Recipe rec5 = new Recipe("", "Zupa krem brokułowo-szpinakowa z ryżem", "Ryż ugotować zgodnie z instrukcją na opakowaniu. Ziemniaka i marchew obrać, umyć i pokroić w kostkę. Dodać do nich różyczki brokułów i umyty szpinak. Całość zalać bulionem i gotować do miękkości. Następnie wszystko zmiksować i w razie potrzeby uzupełnić wodą lub bulionem. Zupę doprawić do smaku solą, pieprzem i czosnkiem, podawać z pokruszonym serem feta i ryżem i łyżeczką oleju lnianego (dodanego bezpośrednio na talerz).", null, RecipeType.SUPPER, 363);
//        Logic.appSyncDb.createNewRecipe(rec1);
//        Logic.appSyncDb.createNewRecipe(rec2);
//        Logic.appSyncDb.createNewRecipe(rec3);
//        Logic.appSyncDb.createNewRecipe(rec4);
//        Logic.appSyncDb.createNewRecipe(rec5);
//    }
}
