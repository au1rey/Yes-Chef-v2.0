package com.example.yeschef.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.util.Log;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.yeschef.R;
import com.example.yeschef.models.FilterParams;
import com.example.yeschef.models.Recipe;
import com.example.yeschef.utils.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScrollingFragment extends Fragment implements FilterBottomSheet.FilterCallback {

    private GridLayout recipeContainer;
    private SearchView searchView;
    private ImageButton delRecipeButton;
    private int recipeCount = 0; // Start with 0 recipes
    private Map<Integer, Recipe> loadedRecipeMap = new HashMap<>();

    // For deletion of recipes
    private boolean isDeleteMode = false;
    private final Map<View, Integer> selectedRecipes = new HashMap<>();
    private Map<Integer, Recipe> recipeMap = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scrolling, container, false);

        recipeCount = 0;

        // Initialize the GridLayout, SearchView, and Button
        recipeContainer = view.findViewById(R.id.recipe_container);
        searchView      = view.findViewById(R.id.searchView);
        delRecipeButton = view.findViewById(R.id.delRecipeButton);


        loadedRecipeMap = JsonUtils.loadRecipeMapFromJson(requireContext(), "recipes.json");

        recipeMap.putAll(loadedRecipeMap); // Populate recipeMap with the loaded data
        // Initially display all recipes
        populateRecipes("", null);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Trigger filtering when the user submits the query
                populateRecipes(query, null);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Trigger filtering as the user types
                populateRecipes(newText, null);
                return true;
            }
        });

        ImageButton filterButton = view.findViewById(R.id.filter_button);

        // Set an OnClickListener to open the filter panel
        filterButton.setOnClickListener(v -> {
            // Open the filter panel
            FilterBottomSheet filterBottomSheet = new FilterBottomSheet();
            filterBottomSheet.setFilterCallback(this);
            filterBottomSheet.show(getParentFragmentManager(), "FilterBottomSheet");
        });

        return view;
    }

    private void populateRecipes(String filter, FilterParams filterParams) {
        // Clear the existing GridLayout
        recipeContainer.removeAllViews();

        // Filter and add recipes that match the filter
        for (Map.Entry<Integer, Recipe> entry : loadedRecipeMap.entrySet()) {
            Integer recipeId = entry.getKey();
            Recipe recipe = entry.getValue();

            // For deletion
            delRecipeButton.setOnClickListener(v -> {
                toggleDeleteMode();
            });
            // Apply filtering logic
            if (!recipe.getTitle().toLowerCase().contains(filter.toLowerCase())) continue;
            if (filterParams != null) {
                if (!filterParams.descriptionTags.isEmpty() && !containsAllTags(filterParams.descriptionTags, recipe.getDescription())) continue;
                if (!filterParams.servingSizeParam.IsValid(recipe.getServingSize())) continue;
                if (!filterParams.calorieParam.IsValid(recipe.getCal())) continue;
                if (!filterParams.proteinParam.IsValid(recipe.getProtein())) continue;
                if (!filterParams.difficulty.isEmpty() && !filterParams.difficulty.contains(recipe.getDifficultyLevel().toString().toLowerCase())) continue;
                if (!filterParams.mealtime.isEmpty() && !filterParams.mealtime.contains(recipe.getMealTime().toString().toLowerCase())) continue;
                if (!filterParams.dietaryOptions.isEmpty() && !matchesDietaryOptions(recipe, filterParams.dietaryOptions)) continue;
                if (!filterParams.ingredients.isEmpty() && !containsAllFilteredStrings(filterParams.ingredients, recipe.getIngredients())) continue;
                if (!filterParams.directions.isEmpty() && !containsAllFilteredStrings(filterParams.directions, recipe.getDirections())) continue;
            }

            addRecipeItem(entry.getKey(), recipe);
        }
    }

    private boolean containsAllTags(List<String> filter, String description) {
        if (filter == null || description == null)
            return false;

        String lowerDescription = description.toLowerCase();

        for (String tag : filter)
            if (!lowerDescription.contains(tag.toLowerCase()))
                return false; // If this, then there was a tag missing

        return true; // Returning here means all tags were found
    }

    private boolean containsAllFilteredStrings(List<String> filter, List<String> recipe) {
        // Convert the recipe list to lowercase for case-insensitive comparison
        List<String> recipeLower = recipe.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        // Check if all items in the filter are present in the recipe
        for (String ingredient : filter) {
            if (!recipeLower.contains(ingredient.toLowerCase())) {
                return false; // Return false if any ingredient is missing
            }
        }

        return true; // Return true if all filter ingredients are found
    }

    private boolean matchesDietaryOptions(Recipe recipe, List<String> options) {
        if (options.contains("vegetarian") && !recipe.getIsVegetarian())
            return false;
        if (options.contains("gluten-free") && !recipe.getIsGlutenFree())
            return false;
        if (options.contains("sugar-free") && !recipe.getIsSugarFree())
            return false;
        return true;
    }

    private void addRecipeItem(int recipeId, Recipe recipe) {
        View recipeItemView = LayoutInflater.from(getContext())
                .inflate(R.layout.recipe_item, recipeContainer, false);

        TextView recipeTitleTextView = recipeItemView.findViewById(R.id.recipe_name);
        recipeTitleTextView.setText(recipe.getTitle());

        ImageButton recipeImageButton = recipeItemView.findViewById(R.id.recipe_image);
        recipeImageButton.setImageURI(recipe.getImage());

        // Set OnClickListener for each recipe item
        recipeImageButton.setOnClickListener(v -> {
            if (isDeleteMode) {
                CardView recipeCardView = recipeItemView.findViewById(R.id.recipe_card_view);

                boolean isSelected = selectedRecipes.containsKey(recipeItemView);

                if (isSelected) {
                    // Deselect and reset background to default (no highlight)
                    recipeCardView.setCardBackgroundColor(getResources().getColor(android.R.color.white)); // Remove border
                    selectedRecipes.remove(recipeItemView); // Unselect recipe
                } else {
                    // Select and apply border (highlight)
                    recipeCardView.setCardBackgroundColor(getResources().getColor(R.color.pink)); // Apply pink border
                    selectedRecipes.put(recipeItemView, recipeId); // Select recipe for deletion
                }
            } else { // Non-delete mode: show recipe details fragment
                RecipeDetailsBottomSheet bottomSheet = new RecipeDetailsBottomSheet();
                bottomSheet.setRecipe(recipe);
                bottomSheet.show(getParentFragmentManager(), "RecipeDetailsBottomSheet");
            }
        });

        // Add the item to the GridLayout
        recipeContainer.addView(recipeItemView);
    }

    @Override
    public void onFilterApplied(FilterParams filterParams) {
        populateRecipes(searchView.getQuery().toString(), filterParams);
    }

    private void deleteSelectedRecipes() {
        // Check if any recipes are selected
        if (selectedRecipes.isEmpty()) {
            return; // If no recipes are selected, do nothing
        }

        // Get the current recipe map from JSON
        Map<Integer, Recipe> loadedRecipeMap = JsonUtils.loadRecipeMapFromJson(requireContext(), "recipes.json");

        // Remove the selected recipes from the map and the UI
        for (Map.Entry<View, Integer> entry : selectedRecipes.entrySet()) {
            View recipeItemView = entry.getKey(); // View representing the recipe item
            Integer recipeId = entry.getValue(); // The ID of the recipe to be deleted

            // Remove the recipe from the UI
            recipeContainer.removeView(recipeItemView); // This will remove the view from the container

            // Remove the recipe from the map
            loadedRecipeMap.remove(recipeId);
        }

        // Rebuild the recipe map with new IDs
        rebuildRecipeMap(loadedRecipeMap);

        // Save the updated map back to the JSON file
        JsonUtils.saveRecipeMapToJson(requireContext(), recipeMap, "recipes.json");

        // After deletion, exit delete mode and reset the icon
        isDeleteMode = false;
        delRecipeButton.setImageResource(R.drawable.ic_delete); // Reset the delete button icon
    }


    private void toggleDeleteMode() {
        if (isDeleteMode) {
            // We're in delete mode already. Show the confirmation dialog if recipes are selected.
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Recipes")
                    .setMessage("Are you sure you want to delete the selected recipes?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete selected recipes
                        deleteSelectedRecipes();
                        dialog.dismiss();
                        // Reset the delete mode and icon
                        isDeleteMode = false;
                        delRecipeButton.setImageResource(R.drawable.ic_delete);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                        // Exit delete mode if the user cancels
                        isDeleteMode = false;
                        delRecipeButton.setImageResource(R.drawable.ic_delete);
                        clearSelection(); // Optional: Clear any selection overlays
                    })
                    .show();
        } else {
            // Enter delete mode
            isDeleteMode = true;
            delRecipeButton.setImageResource(R.drawable.ic_del_forever);

            // Show selection overlays for all recipes
            for (int i = 0; i < recipeContainer.getChildCount(); i++) {
                View recipeItem = recipeContainer.getChildAt(i);
                recipeItem.findViewById(R.id.selection_overlay).setVisibility(View.VISIBLE);
            }
        }
    }

    // Method to clear the selection overlays and reset selections
    private void clearSelection() {
        for (int i = 0; i < recipeContainer.getChildCount(); i++) {
            View recipeItem = recipeContainer.getChildAt(i);
            recipeItem.findViewById(R.id.selection_overlay).setVisibility(View.GONE);
        }
        selectedRecipes.clear(); // Clear the selected recipes map
    }

    private void rebuildRecipeMap(Map<Integer, Recipe> oldMap) {
        Map<Integer, Recipe> newMap = new HashMap<>();
        int newId = 1; // Starting ID value

        // Iterate through the old map and assign new IDs
        for (Map.Entry<Integer, Recipe> entry : oldMap.entrySet()) {
            Recipe recipe = entry.getValue();
            newMap.put(newId++, recipe); // Add to new map with new IDs
        }

        // Update your recipeMap with the re-indexed map
        recipeMap.clear();
        recipeMap.putAll(newMap);
    }
}
