package com.example.yeschef.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yeschef.models.Recipe;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.yeschef.R;

import java.util.Objects;

public class RecipeDetailsBottomSheet extends BottomSheetDialogFragment {

    // Class variables for all UI elements with IDs
    private TextView recipeTitle;
    private ImageView recipeImage;
    private TextView servingsValue;
    private TextView caloriesValue;
    private TextView proteinValue;
    private TextView description;
    private TextView mealtimeValue;
    private TextView difficultyValue;
    private LinearLayout vegetarianContainer;
    private LinearLayout glutenFreeContainer;
    private LinearLayout sugarFreeContainer;
    private TextView ingredientsList;
    private TextView directionsList;
    private Recipe recipe;
    private ImageButton editButton;
    private View dietaryBorder;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("MyTest", "onCreateView called");
        View view = inflater.inflate(R.layout.recipe_details_bottom_sheet, container, false);

        // Initialize all UI elements
        recipeTitle = view.findViewById(R.id.recipe_title);
        recipeImage = view.findViewById(R.id.recipe_image);
        servingsValue = view.findViewById(R.id.details_servings_value);
        caloriesValue = view.findViewById(R.id.details_calories_value);
        proteinValue = view.findViewById(R.id.details_protein_value);
        description = view.findViewById(R.id.details_description);
        mealtimeValue = view.findViewById(R.id.details_mealtime_value);
        difficultyValue = view.findViewById(R.id.details_difficulty_value);
        vegetarianContainer = view.findViewById(R.id.vegetarian_container);
        glutenFreeContainer = view.findViewById(R.id.gluten_free_container);
        sugarFreeContainer = view.findViewById(R.id.sugar_free_container);
        ingredientsList = view.findViewById(R.id.details_ingredients_list);
        directionsList = view.findViewById(R.id.details_directions_list);
        editButton = view.findViewById(R.id.edit_button);
        dietaryBorder = view.findViewById(R.id.dietary_border);

        recipeTitle.setText(recipe.getTitle());
        recipeImage.setImageURI(recipe.getImage());
        servingsValue.setText(formatNumber(recipe.getServingSize()));
        caloriesValue.setText(formatNumber(recipe.getCal()));
        proteinValue.setText(recipe.getProtein() > 0 ? String.format("%d g", recipe.getProtein()) : "N/A");

        description.setText(Objects.equals(recipe.getDescription(), "") ? "Add your own description to make this recipe yours!" : recipe.getDescription());
        mealtimeValue.setText(recipe.getMealTime().toString());
        difficultyValue.setText(recipe.getDifficultyLevel().toString());

        // Show/hide dietary options based on recipe data
        final int visibilty = recipe.getIsVegetarian() ? View.VISIBLE : View.GONE;
        vegetarianContainer.setVisibility(recipe.getIsVegetarian() ? View.VISIBLE : View.GONE);
        glutenFreeContainer.setVisibility(recipe.getIsGlutenFree() ? View.VISIBLE : View.GONE);
        sugarFreeContainer.setVisibility(recipe.getIsSugarFree() ? View.VISIBLE : View.GONE);
        dietaryBorder.setVisibility((recipe.getIsGlutenFree() || recipe.getIsSugarFree() || recipe.getIsVegetarian())
                ? View.VISIBLE : View.GONE);

        // Set the ingredients and directions
        ingredientsList.setText(String.format("• %s", String.join("\n• ", recipe.getIngredients())));
        directionsList.setText(formatDirections(recipe.getDirections()));

        editButton.setOnClickListener((v) -> {
            AddFragment recipeDetailsFragment = new AddFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("recipeId", recipe.getId());
            bundle.putString("recipeTitle", recipe.getTitle());
            bundle.putString("recipeDescription", recipe.getDescription());
            bundle.putInt("servingSize", recipe.getServingSize());
            bundle.putInt("calories", recipe.getCal());
            bundle.putInt("protein", recipe.getProtein());
            bundle.putInt("difficulty", recipe.getDifficultyLevel().ordinal());
            bundle.putInt("mealtime", recipe.getMealTime().ordinal());

            bundle.putBoolean("isVegetarian", recipe.getIsVegetarian());
            bundle.putBoolean("isGlutenFree", recipe.getIsGlutenFree());
            bundle.putBoolean("isSugarFree", recipe.getIsSugarFree());

            bundle.putString("image", recipe.getImage().toString());

            bundle.putStringArrayList("ingredients", recipe.getIngredients());
            bundle.putStringArrayList("directions", recipe.getDirections());

            recipeDetailsFragment.setArguments(bundle);

            // Replace the current fragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, recipeDetailsFragment) // Replace with the ID of your container
                    .addToBackStack(null) // Add to back stack for "Back" navigation
                    .commit();

            dismiss();

        });

        return view;
    }


    // Method to set recipe data into the UI
    public void setRecipe(Recipe recipe) {
        if (recipe == null)
            return;
        this.recipe = recipe;
    }

    private String formatNumber(int number) {
        if (number < 0)
            return "N/A";
        return String.valueOf(number);
    }

    // Helper method to format directions into numbered steps
    private String formatDirections(java.util.List<String> directions) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < directions.size(); i++) {
            formatted.append(i + 1).append(". ").append(directions.get(i)).append("\n");
        }
        return formatted.toString().trim();
    }
}
